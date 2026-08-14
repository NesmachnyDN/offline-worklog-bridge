package io.github.nesmachnydn.worklogbridge.classification;

import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import org.springframework.stereotype.Component;

@Component
public class SourceWorklogMapper {
    private final WorklogClassificationPolicy classificationPolicy;

    public SourceWorklogMapper(WorklogClassificationPolicy classificationPolicy) {
        this.classificationPolicy = classificationPolicy;
    }

    public CapturedWorklog map(RawWorklog raw) {
        return new CapturedWorklog(
                raw.sourceWorklogId(),
                raw.issueKey(),
                raw.issueTitle(),
                raw.workDate(),
                raw.minutes(),
                classificationPolicy.classify(raw));
    }
}
