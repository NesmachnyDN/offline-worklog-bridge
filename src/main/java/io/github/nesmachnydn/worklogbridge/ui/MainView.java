package io.github.nesmachnydn.worklogbridge.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import io.github.nesmachnydn.worklogbridge.domain.DailyAllocation;
import io.github.nesmachnydn.worklogbridge.domain.TimesheetEntry;
import io.github.nesmachnydn.worklogbridge.reconciliation.DailyReconciliation;
import io.github.nesmachnydn.worklogbridge.service.BridgeWorkflowService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Route("")
public class MainView extends VerticalLayout {
    private final BridgeWorkflowService workflow;
    private final Grid<CapturedWorklog> sourceGrid = new Grid<>(CapturedWorklog.class, false);
    private final Grid<DailyReconciliation> reconciliationGrid = new Grid<>(DailyReconciliation.class, false);
    private final Grid<DailyAllocation> allocationGrid = new Grid<>(DailyAllocation.class, false);
    private final Grid<TimesheetEntry> destinationGrid = new Grid<>(TimesheetEntry.class, false);
    private final TextArea commentPreview = new TextArea("Selected destination comment");
    private String snapshotId;

    public MainView(BridgeWorkflowService workflow) {
        this.workflow = workflow;
        setSizeFull();
        setMaxWidth("1500px");
        getStyle().set("margin", "0 auto");

        add(new H1("Offline Worklog Bridge"));
        add(new Paragraph("Capture detailed worklogs in one network context, reconcile the reporting period locally, then publish an aggregated timesheet after switching to a separate network context."));

        Button capture = new Button("Capture synthetic work week", event -> captureWeek());
        Button publish = new Button("Publish preview to demo destination", event -> publish());
        add(new HorizontalLayout(capture, publish));

        configureSourceGrid();
        configureReconciliationGrid();
        configureAllocationGrid();
        configureDestinationGrid();
        commentPreview.setWidthFull();
        commentPreview.setReadOnly(true);
        commentPreview.setMinHeight("110px");

        add(new Paragraph("1. Captured source worklogs"), sourceGrid,
                new Paragraph("2. Workload reconciliation"), reconciliationGrid,
                new Paragraph("3. Allocation preview"), allocationGrid,
                new Paragraph("4. Destination entries"), destinationGrid,
                commentPreview);
        expand(sourceGrid, reconciliationGrid, allocationGrid, destinationGrid);

        workflow.latestSnapshotId().ifPresent(id -> {
            snapshotId = id;
            refresh();
        });
    }

    private void configureSourceGrid() {
        sourceGrid.addColumn(CapturedWorklog::workDate).setHeader("Date").setAutoWidth(true);
        sourceGrid.addColumn(CapturedWorklog::issueKey).setHeader("Issue").setAutoWidth(true);
        sourceGrid.addColumn(CapturedWorklog::issueTitle).setHeader("Source activity").setFlexGrow(1);
        sourceGrid.addColumn(CapturedWorklog::category).setHeader("Category").setAutoWidth(true);
        sourceGrid.addColumn(w -> format(w.minutes())).setHeader("Source time").setAutoWidth(true);
    }

    private void configureReconciliationGrid() {
        reconciliationGrid.addColumn(DailyReconciliation::workDate).setHeader("Date").setAutoWidth(true);
        reconciliationGrid.addColumn(d -> format(d.expectedMinutes())).setHeader("Expected").setAutoWidth(true);
        reconciliationGrid.addColumn(d -> format(d.capturedMinutes())).setHeader("Captured").setAutoWidth(true);
        reconciliationGrid.addColumn(d -> formatDelta(d.deltaMinutes())).setHeader("Delta").setAutoWidth(true);
        reconciliationGrid.addColumn(DailyReconciliation::status).setHeader("Status").setAutoWidth(true);
    }

    private void configureAllocationGrid() {
        allocationGrid.addColumn(DailyAllocation::workDate).setHeader("Date").setAutoWidth(true);
        allocationGrid.addColumn(d -> format(d.sourceMinutes())).setHeader("Source total").setAutoWidth(true);
        allocationGrid.addColumn(d -> format(d.nonDeliveryMinutes())).setHeader("Allocated overhead").setAutoWidth(true);
        allocationGrid.addColumn(d -> d.tasks().stream()
                        .map(t -> t.issueKey() + "=" + format(t.destinationMinutes()))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("BLOCKED"))
                .setHeader("Destination allocation").setFlexGrow(1);
        allocationGrid.addColumn(d -> d.publishable() ? "READY" : "REVIEW REQUIRED").setHeader("Status").setAutoWidth(true);
    }

    private void configureDestinationGrid() {
        destinationGrid.addColumn(TimesheetEntry::workDate).setHeader("Date").setAutoWidth(true);
        destinationGrid.addColumn(e -> format(e.minutes())).setHeader("Timesheet total").setAutoWidth(true);
        destinationGrid.addColumn(e -> e.idempotencyKey().substring(0, 12)).setHeader("Idempotency key").setAutoWidth(true);
        destinationGrid.addSelectionListener(event -> event.getFirstSelectedItem()
                .ifPresent(entry -> commentPreview.setValue(entry.comment())));
    }

    private void captureWeek() {
        LocalDate start = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        snapshotId = workflow.capture(start, start.plusDays(6));
        refresh();
        Notification.show("Snapshot captured. Disconnect the source network before publishing in a real deployment.");
    }

    private void publish() {
        if (snapshotId == null) {
            Notification.show("Capture a snapshot first");
            return;
        }
        try {
            var result = workflow.publish(snapshotId);
            long duplicates = result.stream().filter(BridgeWorkflowService.PublishResult::alreadyPublished).count();
            Notification.show("Published " + result.size() + " day(s); idempotent repeats: " + duplicates);
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    private void refresh() {
        sourceGrid.setItems(workflow.captured(snapshotId));
        reconciliationGrid.setItems(workflow.reconciliation(snapshotId).days());
        allocationGrid.setItems(workflow.plan(snapshotId));
        try {
            var preview = workflow.destinationPreview(snapshotId);
            destinationGrid.setItems(preview);
            preview.stream().findFirst().ifPresent(entry -> commentPreview.setValue(entry.comment()));
        } catch (RuntimeException ex) {
            destinationGrid.setItems(java.util.List.of());
            commentPreview.setValue(ex.getMessage());
        }
    }

    private static String format(int minutes) {
        return "%dh %02dm".formatted(minutes / 60, minutes % 60);
    }

    private static String formatDelta(int minutes) {
        if (minutes == 0) return "0";
        int absolute = Math.abs(minutes);
        return (minutes > 0 ? "+" : "-") + format(absolute);
    }
}
