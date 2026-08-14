package io.github.nesmachnydn.worklogbridge.reconciliation;

import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkloadReconciliationService {
    public static final int STANDARD_WORKDAY_MINUTES = 8 * 60;

    public ReconciliationReport reconcile(LocalDate startInclusive, LocalDate endInclusive, List<CapturedWorklog> worklogs) {
        if (endInclusive.isBefore(startInclusive)) throw new IllegalArgumentException("end date must not precede start date");
        Map<LocalDate, Integer> capturedByDay = worklogs.stream().collect(Collectors.groupingBy(
                CapturedWorklog::workDate, Collectors.summingInt(CapturedWorklog::minutes)));

        List<DailyReconciliation> days = new ArrayList<>();
        int expectedTotal = 0;
        int capturedTotal = 0;
        for (LocalDate day = startInclusive; !day.isAfter(endInclusive); day = day.plusDays(1)) {
            int expected = isWeekday(day) ? STANDARD_WORKDAY_MINUTES : 0;
            int captured = capturedByDay.getOrDefault(day, 0);
            int delta = captured - expected;
            ReconciliationStatus status = delta == 0
                    ? ReconciliationStatus.OK
                    : delta < 0 ? ReconciliationStatus.UNDER_REPORTED : ReconciliationStatus.OVER_REPORTED;
            days.add(new DailyReconciliation(day, expected, captured, delta, status));
            expectedTotal += expected;
            capturedTotal += captured;
        }
        return new ReconciliationReport(expectedTotal, capturedTotal, capturedTotal - expectedTotal, days);
    }

    private boolean isWeekday(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
}
