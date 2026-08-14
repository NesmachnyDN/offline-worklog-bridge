package io.github.nesmachnydn.worklogbridge.domain;

import java.time.LocalDate;
import java.util.List;

public record DailyAllocation(
        LocalDate workDate,
        int sourceMinutes,
        int deliveryMinutes,
        int nonDeliveryMinutes,
        List<AllocatedTask> tasks,
        boolean publishable,
        String warning) {

    public DailyAllocation {
        tasks = List.copyOf(tasks);
        int allocated = tasks.stream().mapToInt(AllocatedTask::destinationMinutes).sum();
        if (publishable && allocated != sourceMinutes) {
            throw new IllegalArgumentException("publishable allocation must conserve all source minutes");
        }
    }
}
