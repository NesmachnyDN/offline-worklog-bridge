package io.github.nesmachnydn.worklogbridge.destination;

import io.github.nesmachnydn.worklogbridge.adapter.DestinationTimesheetPort;
import io.github.nesmachnydn.worklogbridge.domain.PublishReceipt;
import io.github.nesmachnydn.worklogbridge.domain.TimesheetEntry;
import io.github.nesmachnydn.worklogbridge.persistence.DemoTimesheetEntity;
import io.github.nesmachnydn.worklogbridge.persistence.DemoTimesheetRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
public class LocalDemoDestinationAdapter implements DestinationTimesheetPort {
    private final DemoTimesheetRepository repository;
    public LocalDemoDestinationAdapter(DemoTimesheetRepository repository) { this.repository = repository; }
    @Override public String name() { return "Local demo weekly timesheet"; }
    @Override @Transactional
    public PublishReceipt publish(TimesheetEntry entry) {
        return repository.findByIdempotencyKey(entry.idempotencyKey())
                .map(existing -> new PublishReceipt(existing.getExternalReference(), true))
                .orElseGet(() -> {
                    String reference = "DEMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    repository.save(new DemoTimesheetEntity(entry.workDate(), entry.minutes(), entry.comment(), entry.idempotencyKey(), reference));
                    return new PublishReceipt(reference, false);
                });
    }
}
