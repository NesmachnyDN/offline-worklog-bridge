package io.github.nesmachnydn.worklogbridge.service;

import io.github.nesmachnydn.worklogbridge.domain.AllocatedTask;
import io.github.nesmachnydn.worklogbridge.domain.DailyAllocation;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceabilityCommentServiceTest {
    @Test void commentKeepsDestinationEntryTraceableToDeliveryTasksAndAllocatedOverhead() {
        var allocation = new DailyAllocation(LocalDate.of(2026, 8, 10), 480, 300, 180,
                List.of(new AllocatedTask("DEV-101", "Implement capability", 180, 108, 288, List.of("wl-1")),
                        new AllocatedTask("DEV-102", "Review capability", 120, 72, 192, List.of("wl-2"))), true, null);
        String comment = new TraceabilityCommentService().build(allocation);
        assertTrue(comment.contains("DEV-101")); assertTrue(comment.contains("4h 48m"));
        assertTrue(comment.contains("allocated non-delivery time")); assertTrue(comment.contains("DEV-102"));
    }
}
