package io.github.nesmachnydn.worklogbridge.domain;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProportionalAllocationPolicy implements AllocationPolicy {
    @Override
    public DailyAllocation allocate(List<CapturedWorklog> worklogs) {
        if (worklogs == null || worklogs.isEmpty()) throw new IllegalArgumentException("worklogs must not be empty");
        LocalDate date = worklogs.getFirst().workDate();
        if (worklogs.stream().anyMatch(w -> !date.equals(w.workDate()))) {
            throw new IllegalArgumentException("allocation is performed one day at a time");
        }

        int sourceMinutes = worklogs.stream().mapToInt(CapturedWorklog::minutes).sum();
        int deliveryMinutes = worklogs.stream().filter(w -> w.category() == WorklogCategory.DELIVERY).mapToInt(CapturedWorklog::minutes).sum();
        int nonDeliveryMinutes = sourceMinutes - deliveryMinutes;

        Map<TaskKey, List<CapturedWorklog>> deliveryByTask = worklogs.stream()
                .filter(w -> w.category() == WorklogCategory.DELIVERY)
                .collect(Collectors.groupingBy(w -> new TaskKey(w.issueKey(), w.issueTitle()), LinkedHashMap::new, Collectors.toList()));

        if (deliveryByTask.isEmpty()) {
            return new DailyAllocation(date, sourceMinutes, 0, nonDeliveryMinutes, List.of(), false,
                    "The day contains no delivery work. Non-delivery time cannot be allocated without inventing a target task.");
        }

        Map<TaskKey, Integer> baseMinutes = new LinkedHashMap<>();
        deliveryByTask.forEach((key, values) -> baseMinutes.put(key, values.stream().mapToInt(CapturedWorklog::minutes).sum()));
        Map<TaskKey, Integer> overhead = allocateByLargestRemainder(baseMinutes, nonDeliveryMinutes, deliveryMinutes);

        List<AllocatedTask> tasks = baseMinutes.entrySet().stream().map(entry -> {
            TaskKey key = entry.getKey();
            int base = entry.getValue();
            int extra = overhead.getOrDefault(key, 0);
            List<String> sourceIds = deliveryByTask.get(key).stream().map(CapturedWorklog::sourceWorklogId).toList();
            return new AllocatedTask(key.issueKey(), key.issueTitle(), base, extra, base + extra, sourceIds);
        }).toList();

        return new DailyAllocation(date, sourceMinutes, deliveryMinutes, nonDeliveryMinutes, tasks, true, null);
    }

    private Map<TaskKey, Integer> allocateByLargestRemainder(Map<TaskKey, Integer> baseMinutes, int overheadMinutes, int deliveryMinutes) {
        Map<TaskKey, Integer> result = new LinkedHashMap<>();
        if (overheadMinutes == 0) {
            baseMinutes.keySet().forEach(key -> result.put(key, 0));
            return result;
        }
        List<Share> shares = new ArrayList<>();
        int allocated = 0;
        for (var entry : baseMinutes.entrySet()) {
            double exact = ((double) entry.getValue() * overheadMinutes) / deliveryMinutes;
            int floor = (int) Math.floor(exact);
            allocated += floor;
            result.put(entry.getKey(), floor);
            shares.add(new Share(entry.getKey(), exact - floor));
        }
        shares.sort(Comparator.comparingDouble(Share::remainder).reversed().thenComparing(share -> share.key().issueKey()));
        int remainder = overheadMinutes - allocated;
        for (int i = 0; i < remainder; i++) {
            TaskKey key = shares.get(i % shares.size()).key();
            result.compute(key, (ignored, current) -> current + 1);
        }
        return result;
    }

    private record TaskKey(String issueKey, String issueTitle) {}
    private record Share(TaskKey key, double remainder) {}
}
