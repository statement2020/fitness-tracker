package uk.co.devinity.services;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UserStatsServiceImpl implements UserStatsService {

    private final EntryRepository entryRepository;

    public UserStatsServiceImpl(EntryRepository entryService) {
        this.entryRepository = entryService;
    }

    @Override
    public double getWeightLossThisWeek(User user) {
        LocalDate now = LocalDate.now();
        LocalDate weekAgo = now.minusWeeks(1);

        List<Entry> weekEntries = entryRepository.findByUserOrderByDateAsc(user).stream()
                .filter(e -> !e.getDate().isBefore(weekAgo) && !e.getDate().isAfter(now))
                .sorted(Comparator.comparing(Entry::getDate))
                .toList();

        if (weekEntries.size() < 2) {
            return 0.0;
        }

        double start = weekEntries.get(0).getWeight();
        double end = weekEntries.get(weekEntries.size() - 1).getWeight();
        return start - end;
    }

    @Override
    public double getWeightLossOverall(User user) {
        List<Entry> entries = entryRepository.findByUserOrderByDateAsc(user).stream()
                .sorted(Comparator.comparing(Entry::getDate))
                .toList();

        if (entries.size() < 2) return 0.0;

        double start = entries.get(0).getWeight();
        double end = entries.get(entries.size() - 1).getWeight();
        return start - end;
    }

    @Override
    public double getAverageWeightLossPerWeek(User user) {
        List<Entry> entries = entryRepository.findByUserOrderByDateAsc(user).stream()
                .sorted(Comparator.comparing(Entry::getDate))
                .toList();

        if (entries.size() < 2) return 0.0;

        double totalLoss = getWeightLossOverall(user);
        long weeks = ChronoUnit.WEEKS.between(entries.get(0).getDate(), entries.get(entries.size() - 1).getDate());
        return weeks > 0 ? totalLoss / weeks : 0.0;
    }

    @Override
    public double getAverageWeightLossPerDay(User user) {
        List<Entry> entries = entryRepository.findByUserOrderByDateAsc(user).stream()
                .sorted(Comparator.comparing(Entry::getDate))
                .toList();

        if (entries.size() < 2) return 0.0;

        double totalLoss = getWeightLossOverall(user);
        long days = ChronoUnit.DAYS.between(entries.get(0).getDate(), entries.get(entries.size() - 1).getDate());
        return days > 0 ? totalLoss / days : 0.0;
    }

    @Override
    public double getAverageCaloriesBurntPerDay(User user) {
        return averageByDay(user, true);
    }

    @Override
    public double getAverageCaloriesConsumedPerDay(User user) {
        return averageByDay(user, false);
    }


    @Override
    public void getAllStats(Model model, List<User> users) {
        final Map<Long, Map<String, Double>> userStats = new HashMap<>();

        final var executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

        try {
            for (User user : users) {
                Map<String, CompletableFuture<Double>> futures = new HashMap<>();

                futures.put("weightLossThisWeek", CompletableFuture.supplyAsync(() -> getWeightLossThisWeek(user), executor));
                futures.put("weightLossOverall", CompletableFuture.supplyAsync(() -> getWeightLossOverall(user), executor));
                futures.put("avgLossPerWeek", CompletableFuture.supplyAsync(() -> getAverageWeightLossPerWeek(user), executor));
                futures.put("avgLossPerDay", CompletableFuture.supplyAsync(() -> getAverageWeightLossPerDay(user), executor));
                futures.put("avgBurntPerDay", CompletableFuture.supplyAsync(() -> getAverageCaloriesBurntPerDay(user), executor));
                futures.put("avgConsumedPerDay", CompletableFuture.supplyAsync(() -> getAverageCaloriesConsumedPerDay(user), executor));

                // Block and collect results
                Map<String, Double> stats = new HashMap<>();
                for (var entry : futures.entrySet()) {
                    stats.put(entry.getKey(), entry.getValue().join()); // join() waits for completion
                }

                userStats.put(user.getId(), stats);
            }
        } finally {
            executor.shutdown();
        }

        model.addAttribute("users", users);
        model.addAttribute("userStats", userStats);
    }


    private double averageByDay(User user, boolean burnt) {
        List<Entry> entries = entryRepository.findByUserOrderByDateAsc(user);
        if (entries.isEmpty()) return 0.0;

        OptionalDouble avg = entries.stream()
                .mapToDouble(e -> burnt ? e.getCaloriesBurnt() : e.getCaloriesConsumed())
                .average();

        return avg.orElse(0.0);
    }
}
