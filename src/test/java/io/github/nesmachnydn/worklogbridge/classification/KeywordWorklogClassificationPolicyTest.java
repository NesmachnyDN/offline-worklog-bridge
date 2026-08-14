package io.github.nesmachnydn.worklogbridge.classification;

import io.github.nesmachnydn.worklogbridge.domain.WorklogCategory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeywordWorklogClassificationPolicyTest {
    private final KeywordWorklogClassificationPolicy policy =
            new KeywordWorklogClassificationPolicy("planning,meeting,retro,daily,scrum");

    @Test
    void classifiesTeamOverheadFromConfigurableKeywords() {
        assertEquals(WorklogCategory.NON_DELIVERY,
                policy.classify(raw("TEAM-1", "Sprint planning and estimation")));
        assertEquals(WorklogCategory.NON_DELIVERY,
                policy.classify(raw("TEAM-MEETING", "Coordination")));
    }

    @Test
    void leavesDeliveryIssueAsDelivery() {
        assertEquals(WorklogCategory.DELIVERY,
                policy.classify(raw("DEV-101", "Implement payment validation")));
    }

    private RawWorklog raw(String key, String title) {
        return new RawWorklog("wl-1", key, title, LocalDate.of(2026, 8, 10), 60);
    }
}
