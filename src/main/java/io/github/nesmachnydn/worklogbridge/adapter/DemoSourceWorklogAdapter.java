package io.github.nesmachnydn.worklogbridge.adapter;

import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import io.github.nesmachnydn.worklogbridge.domain.WorklogCategory;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoSourceWorklogAdapter implements SourceWorklogPort {
    @Override public String name() { return "Synthetic source tracker"; }

    @Override
    public List<CapturedWorklog> capture(LocalDate startInclusive, LocalDate endInclusive) {
        List<CapturedWorklog> result = new ArrayList<>();
        LocalDate day = startInclusive;
        int sequence = 1;
        while (!day.isAfter(endInclusive)) {
            if (day.getDayOfWeek().getValue() <= 5) {
                String suffix = String.format("%03d", sequence++);
                result.add(new CapturedWorklog("WL-" + suffix + "-A", "ARCH-" + suffix, "Implement integration capability", day, 300, WorklogCategory.DELIVERY));
                result.add(new CapturedWorklog("WL-" + suffix + "-B", "ARCH-" + suffix + "R", "Review architecture and code", day, 90, WorklogCategory.DELIVERY));
                result.add(new CapturedWorklog("WL-" + suffix + "-P", "TEAM-PLANNING", "Sprint planning", day, 30, WorklogCategory.NON_DELIVERY));
                result.add(new CapturedWorklog("WL-" + suffix + "-M", "TEAM-MEETING", "Team coordination meeting", day, 60, WorklogCategory.NON_DELIVERY));
            }
            day = day.plusDays(1);
        }
        return result;
    }
}
