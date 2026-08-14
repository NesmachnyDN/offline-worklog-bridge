package io.github.nesmachnydn.worklogbridge.domain;

import java.time.LocalDate;

public record TimesheetEntry(LocalDate workDate, int minutes, String comment, String idempotencyKey) {
}
