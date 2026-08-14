package io.github.nesmachnydn.worklogbridge.service;

import io.github.nesmachnydn.worklogbridge.adapter.DestinationTimesheetPort;
import io.github.nesmachnydn.worklogbridge.adapter.SourceWorklogPort;
import io.github.nesmachnydn.worklogbridge.domain.AllocationPolicy;
import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import io.github.nesmachnydn.worklogbridge.domain.DailyAllocation;
import io.github.nesmachnydn.worklogbridge.domain.PublishReceipt;
import io.github.nesmachnydn.worklogbridge.domain.TimesheetEntry;
import io.github.nesmachnydn.worklogbridge.persistence.CapturedWorklogEntity;
import io.github.nesmachnydn.worklogbridge.persistence.CapturedWorklogRepository;
import io.github.nesmachnydn.worklogbridge.persistence.PublishRecordEntity;
import io.github.nesmachnydn.worklogbridge.persistence.PublishRecordRepository;
import io.github.nesmachnydn.worklogbridge.persistence.WorklogSnapshotEntity;
import io.github.nesmachnydn.worklogbridge.persistence.WorklogSnapshotRepository;
import io.github.nesmachnydn.worklogbridge.reconciliation.ReconciliationReport;
import io.github.nesmachnydn.worklogbridge.reconciliation.WorkloadReconciliationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BridgeWorkflowService {
    private final SourceWorklogPort source;
    private final DestinationTimesheetPort destination;
    private final WorklogSnapshotRepository snapshotRepository;
    private final CapturedWorklogRepository worklogRepository;
    private final PublishRecordRepository publishRepository;
    private final AllocationPolicy allocationPolicy;
    private final TraceabilityCommentService commentService;
    private final IdempotencyKeyService idempotencyKeyService;
    private final WorkloadReconciliationService reconciliationService;

    public BridgeWorkflowService(SourceWorklogPort source,
                                 DestinationTimesheetPort destination,
                                 WorklogSnapshotRepository snapshotRepository,
                                 CapturedWorklogRepository worklogRepository,
                                 PublishRecordRepository publishRepository,
                                 AllocationPolicy allocationPolicy,
                                 TraceabilityCommentService commentService,
                                 IdempotencyKeyService idempotencyKeyService,
                                 WorkloadReconciliationService reconciliationService) {
        this.source = source;
        this.destination = destination;
        this.snapshotRepository = snapshotRepository;
        this.worklogRepository = worklogRepository;
        this.publishRepository = publishRepository;
        this.allocationPolicy = allocationPolicy;
        this.commentService = commentService;
        this.idempotencyKeyService = idempotencyKeyService;
        this.reconciliationService = reconciliationService;
    }

    @Transactional
    public String capture(LocalDate startInclusive, LocalDate endInclusive) {
        if (endInclusive.isBefore(startInclusive)) {
            throw new IllegalArgumentException("end date must not precede start date");
        }
        List<CapturedWorklog> worklogs = source.capture(startInclusive, endInclusive);
        String snapshotId = UUID.randomUUID().toString();
        snapshotRepository.save(new WorklogSnapshotEntity(snapshotId, startInclusive, endInclusive, Instant.now(), source.name()));
        worklogRepository.saveAll(worklogs.stream().map(worklog -> new CapturedWorklogEntity(snapshotId, worklog)).toList());
        return snapshotId;
    }

    @Transactional(readOnly = true)
    public Optional<String> latestSnapshotId() {
        WorklogSnapshotEntity entity = snapshotRepository.findTopByOrderByCapturedAtDesc();
        return Optional.ofNullable(entity).map(WorklogSnapshotEntity::getId);
    }

    @Transactional(readOnly = true)
    public List<CapturedWorklog> captured(String snapshotId) {
        return worklogRepository.findBySnapshotIdOrderByWorkDateAscIdAsc(snapshotId).stream()
                .map(CapturedWorklogEntity::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReconciliationReport reconciliation(String snapshotId) {
        WorklogSnapshotEntity snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown snapshot: " + snapshotId));
        return reconciliationService.reconcile(snapshot.getStartDate(), snapshot.getEndDate(), captured(snapshotId));
    }

    @Transactional(readOnly = true)
    public List<DailyAllocation> plan(String snapshotId) {
        return captured(snapshotId).stream()
                .collect(Collectors.groupingBy(CapturedWorklog::workDate, TreeMap::new, Collectors.toList()))
                .values().stream()
                .map(allocationPolicy::allocate)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimesheetEntry> destinationPreview(String snapshotId) {
        return plan(snapshotId).stream()
                .map(allocation -> toTimesheetEntry(snapshotId, allocation))
                .toList();
    }

    @Transactional
    public List<PublishResult> publish(String snapshotId) {
        List<DailyAllocation> plan = plan(snapshotId);
        if (plan.stream().anyMatch(day -> !day.publishable())) {
            throw new IllegalStateException("Cannot publish: at least one day has non-delivery time but no delivery target task");
        }

        List<PublishResult> results = new ArrayList<>();
        for (DailyAllocation allocation : plan) {
            TimesheetEntry entry = toTimesheetEntry(snapshotId, allocation);
            var existing = publishRepository.findByIdempotencyKey(entry.idempotencyKey());
            if (existing.isPresent()) {
                results.add(new PublishResult(entry.workDate(), existing.get().getDestinationReference(), true));
                continue;
            }
            PublishReceipt receipt = destination.publish(entry);
            publishRepository.save(new PublishRecordEntity(
                    snapshotId,
                    entry.workDate(),
                    entry.minutes(),
                    entry.comment(),
                    entry.idempotencyKey(),
                    destination.name(),
                    receipt.destinationReference(),
                    Instant.now()));
            results.add(new PublishResult(entry.workDate(), receipt.destinationReference(), receipt.alreadyExisted()));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<PublishRecordEntity> publishHistory() {
        return publishRepository.findTop50ByOrderByPublishedAtDesc();
    }

    private TimesheetEntry toTimesheetEntry(String snapshotId, DailyAllocation allocation) {
        if (!allocation.publishable()) {
            throw new IllegalStateException(allocation.warning());
        }
        String comment = commentService.build(allocation);
        String key = idempotencyKeyService.forEntry(snapshotId, allocation.workDate(), allocation.sourceMinutes(), comment);
        return new TimesheetEntry(allocation.workDate(), allocation.sourceMinutes(), comment, key);
    }

    public record PublishResult(LocalDate workDate, String destinationReference, boolean alreadyPublished) {}
}
