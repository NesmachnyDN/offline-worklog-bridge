package io.github.nesmachnydn.worklogbridge.classification;

import io.github.nesmachnydn.worklogbridge.domain.WorklogCategory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class KeywordWorklogClassificationPolicy implements WorklogClassificationPolicy {
    private final List<String> nonDeliveryKeywords;

    public KeywordWorklogClassificationPolicy(
            @Value("${bridge.classification.non-delivery-keywords:planning,meeting,retro,daily,scrum}") String keywords) {
        this.nonDeliveryKeywords = Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .toList();
    }

    @Override
    public WorklogCategory classify(RawWorklog worklog) {
        String searchable = (worklog.issueKey() + " " + worklog.issueTitle()).toLowerCase(Locale.ROOT);
        return nonDeliveryKeywords.stream().anyMatch(searchable::contains)
                ? WorklogCategory.NON_DELIVERY
                : WorklogCategory.DELIVERY;
    }
}
