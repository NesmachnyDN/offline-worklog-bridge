package io.github.nesmachnydn.worklogbridge.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProportionalAllocationPolicyTest {
    private final ProportionalAllocationPolicy policy = new ProportionalAllocationPolicy();
    private final LocalDate day = LocalDate.of(2026, 8, 10);

    @Test void allocatesNonDeliveryTimeProportionallyAndConservesMinutes() {
        var result = policy.allocate(List.of(worklog("1", "DEV-1", 180, WorklogCategory.DELIVERY), worklog("2", "DEV-2", 120, WorklogCategory.DELIVERY), worklog("3", "TEAM", 180, WorklogCategory.NON_DELIVERY)));
        assertTrue(result.publishable()); assertEquals(480, result.sourceMinutes()); assertEquals(180, result.nonDeliveryMinutes());
        assertEquals(288, result.tasks().get(0).destinationMinutes()); assertEquals(192, result.tasks().get(1).destinationMinutes());
        assertEquals(480, result.tasks().stream().mapToInt(AllocatedTask::destinationMinutes).sum());
    }

    @Test void largestRemainderKeepsRoundingExact() {
        var result = policy.allocate(List.of(worklog("1", "DEV-1", 240, WorklogCategory.DELIVERY), worklog("2", "DEV-2", 180, WorklogCategory.DELIVERY), worklog("3", "TEAM", 60, WorklogCategory.NON_DELIVERY)));
        assertEquals(34, result.tasks().get(0).allocatedNonDeliveryMinutes()); assertEquals(26, result.tasks().get(1).allocatedNonDeliveryMinutes());
        assertEquals(480, result.tasks().stream().mapToInt(AllocatedTask::destinationMinutes).sum());
    }

    @Test void refusesToInventDeliveryTaskWhenDayContainsOnlyOverhead() {
        var result = policy.allocate(List.of(worklog("1", "TEAM", 480, WorklogCategory.NON_DELIVERY)));
        assertFalse(result.publishable()); assertTrue(result.tasks().isEmpty()); assertNotNull(result.warning());
    }

    @Test void rejectsMixedDates() {
        assertThrows(IllegalArgumentException.class, () -> policy.allocate(List.of(worklog("1", "DEV-1", 60, WorklogCategory.DELIVERY),
                new CapturedWorklog("2", "DEV-2", "Activity", day.plusDays(1), 60, WorklogCategory.DELIVERY))));
    }

    private CapturedWorklog worklog(String id, String issue, int minutes, WorklogCategory category) {
        return new CapturedWorklog(id, issue, "Activity " + issue, day, minutes, category);
    }
}
