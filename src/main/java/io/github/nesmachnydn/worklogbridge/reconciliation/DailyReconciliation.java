package io.github.nesmachnydn.worklogbridge.reconciliation;

import java.time.LocalDate;

public record DailyReconciliation(
        LocalDate workDate,
        int expectedMinutes,
        int capturedMinutes,
        int deltaMinutes,
        ReconciliationStatus status) {
}
