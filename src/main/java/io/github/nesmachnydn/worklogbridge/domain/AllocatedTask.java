package io.github.nesmachnydn.worklogbridge.domain;

import java.util.List;

public record AllocatedTask(
        String issueKey,
        String issueTitle,
        int deliveryMinutes,
        int allocatedNonDeliveryMinutes,
        int destinationMinutes,
        List<String> sourceWorklogIds) {

    public AllocatedTask {
        sourceWorklogIds = List.copyOf(sourceWorklogIds);
        if (destinationMinutes != deliveryMinutes + allocatedNonDeliveryMinutes) {
            throw new IllegalArgumentException("destination minutes must conserve delivery + allocated time");
        }
    }
}
