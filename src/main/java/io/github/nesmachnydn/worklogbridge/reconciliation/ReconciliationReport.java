package io.github.nesmachnydn.worklogbridge.reconciliation;

import java.util.List;

public record ReconciliationReport(
        int expectedMinutes,
        int capturedMinutes,
        int deltaMinutes,
        List<DailyReconciliation> days) {

    public ReconciliationReport {
        days = List.copyOf(days);
    }

    public boolean complete() {
        return days.stream().allMatch(day -> day.status() == ReconciliationStatus.OK);
    }
}
