package io.github.nesmachnydn.worklogbridge.domain;

import java.util.List;

public interface AllocationPolicy {
    DailyAllocation allocate(List<CapturedWorklog> worklogs);
}
