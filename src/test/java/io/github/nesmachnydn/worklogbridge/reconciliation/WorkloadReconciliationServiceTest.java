package io.github.nesmachnydn.worklogbridge.reconciliation;

import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import io.github.nesmachnydn.worklogbridge.domain.WorklogCategory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadReconciliationServiceTest {
    private final WorkloadReconciliationService service = new WorkloadReconciliationService();

    @Test
    void completeWorkWeekReconcilesExactly() {
        LocalDate monday = LocalDate.of(2026, 8, 10);
        List<CapturedWorklog> logs = monday.datesUntil(monday.plusDays(5))
                .map(day -> new CapturedWorklog("wl-" + day, "DEV-1", "Delivery", day, 480, WorklogCategory.DELIVERY))
                .toList();

        var report = service.reconcile(monday, monday.plusDays(6), logs);
        assertTrue(report.complete());
        assertEquals(2400, report.expectedMinutes());
        assertEquals(2400, report.capturedMinutes());
        assertEquals(0, report.deltaMinutes());
    }

    @Test
    void missingTimeIsVisibleWithoutChangingSourceData() {
        LocalDate monday = LocalDate.of(2026, 8, 10);
        var logs = List.of(new CapturedWorklog("wl-1", "DEV-1", "Delivery", monday, 420, WorklogCategory.DELIVERY));
        var report = service.reconcile(monday, monday, logs);
        assertFalse(report.complete());
        assertEquals(ReconciliationStatus.UNDER_REPORTED, report.days().getFirst().status());
        assertEquals(-60, report.days().getFirst().deltaMinutes());
    }

    @Test
    void weekendTimeIsReportedAsOverExpectedRatherThanDiscarded() {
        LocalDate saturday = LocalDate.of(2026, 8, 15);
        var logs = List.of(new CapturedWorklog("wl-1", "DEV-1", "Delivery", saturday, 120, WorklogCategory.DELIVERY));
        var report = service.reconcile(saturday, saturday, logs);
        assertEquals(ReconciliationStatus.OVER_REPORTED, report.days().getFirst().status());
        assertEquals(120, report.capturedMinutes());
    }
}
