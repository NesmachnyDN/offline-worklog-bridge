package io.github.nesmachnydn.worklogbridge.adapter;

import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import java.time.LocalDate;
import java.util.List;

public interface SourceWorklogPort {
    String name();
    List<CapturedWorklog> capture(LocalDate startInclusive, LocalDate endInclusive);
}
