package io.github.nesmachnydn.worklogbridge.adapter;

import io.github.nesmachnydn.worklogbridge.classification.RawWorklog;
import io.github.nesmachnydn.worklogbridge.classification.SourceWorklogMapper;
import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoSourceWorklogAdapter implements SourceWorklogPort {
    private final SourceWorklogMapper mapper;

    public DemoSourceWorklogAdapter(SourceWorklogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String name() {
        return "Synthetic source tracker";
    }

    @Override
    public List<CapturedWorklog> capture(LocalDate startInclusive, LocalDate endInclusive) {
        List<RawWorklog> raw = new ArrayList<>();
        LocalDate day = startInclusive;
        int sequence = 1;
        while (!day.isAfter(endInclusive)) {
            if (day.getDayOfWeek().getValue() <= 5) {
                String suffix = String.format("%03d", sequence++);
                raw.add(new RawWorklog("WL-" + suffix + "-A", "ARCH-" + suffix,
                        "Implement integration capability", day, 300));
                raw.add(new RawWorklog("WL-" + suffix + "-B", "ARCH-" + suffix + "R",
                        "Review architecture and code", day, 90));
                raw.add(new RawWorklog("WL-" + suffix + "-P", "TEAM-PLANNING",
                        "Sprint planning", day, 30));
                raw.add(new RawWorklog("WL-" + suffix + "-M", "TEAM-MEETING",
                        "Team coordination meeting", day, 60));
            }
            day = day.plusDays(1);
        }
        return raw.stream().map(mapper::map).toList();
    }
}
