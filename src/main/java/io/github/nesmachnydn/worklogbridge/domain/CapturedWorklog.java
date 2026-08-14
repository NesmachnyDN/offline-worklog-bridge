package io.github.nesmachnydn.worklogbridge.domain;

import java.time.LocalDate;

public record CapturedWorklog(
        String sourceWorklogId,
        String issueKey,
        String issueTitle,
        LocalDate workDate,
        int minutes,
        WorklogCategory category) {

    public CapturedWorklog {
        if (sourceWorklogId == null || sourceWorklogId.isBlank()) throw new IllegalArgumentException("sourceWorklogId is required");
        if (issueKey == null || issueKey.isBlank()) throw new IllegalArgumentException("issueKey is required");
        if (issueTitle == null || issueTitle.isBlank()) throw new IllegalArgumentException("issueTitle is required");
        if (workDate == null) throw new IllegalArgumentException("workDate is required");
        if (minutes <= 0) throw new IllegalArgumentException("minutes must be positive");
        if (category == null) throw new IllegalArgumentException("category is required");
    }
}
