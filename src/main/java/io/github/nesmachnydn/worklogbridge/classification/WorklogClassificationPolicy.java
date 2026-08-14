package io.github.nesmachnydn.worklogbridge.classification;

import io.github.nesmachnydn.worklogbridge.domain.WorklogCategory;

public interface WorklogClassificationPolicy {
    WorklogCategory classify(RawWorklog worklog);
}
