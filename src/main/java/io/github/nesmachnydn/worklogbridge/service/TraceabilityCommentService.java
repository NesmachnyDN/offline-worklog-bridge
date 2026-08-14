package io.github.nesmachnydn.worklogbridge.service;

import io.github.nesmachnydn.worklogbridge.domain.AllocatedTask;
import io.github.nesmachnydn.worklogbridge.domain.DailyAllocation;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class TraceabilityCommentService {
    public String build(DailyAllocation allocation) {
        return allocation.tasks().stream().map(this::line).collect(Collectors.joining("; "));
    }
    private String line(AllocatedTask task) {
        String added = task.allocatedNonDeliveryMinutes() == 0 ? "" : ", includes " + format(task.allocatedNonDeliveryMinutes()) + " allocated non-delivery time";
        return task.issueKey() + " — " + format(task.destinationMinutes()) + " — " + task.issueTitle() + added;
    }
    static String format(int minutes) { return "%dh %02dm".formatted(minutes / 60, minutes % 60); }
}
