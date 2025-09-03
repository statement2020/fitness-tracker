package uk.co.devinity.services;

import org.springframework.stereotype.Service;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EntryServiceImpl implements EntryService {

    private final EntryRepository entryRepository;
    private final BmrService bmrService;
    private final BmiCalculator bmiCalculator;

    public EntryServiceImpl(EntryRepository entryRepository,
                            BmrService bmrService,
                            BmiCalculator bmiCalculator) {
        this.entryRepository = entryRepository;
        this.bmrService = bmrService;
        this.bmiCalculator = bmiCalculator;
    }

    @Override
    public List<Map<String, Object>> getEntriesForUser(User user) {
        List<Entry> entries = entryRepository.findByUserOrderByDateAsc(user).stream()
                .collect(Collectors.toMap(
                        Entry::getDate, // key: just the date
                        e -> e,                         // value: the entry
                        (existing, replacement) -> replacement // keep the last entry if duplicate
                ))
                .values().stream()
                .sorted(Comparator.comparing(Entry::getDate))
                .toList();

        List<Map<String, Object>> entryData = new ArrayList<>();
        for (Entry e : entries) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", e.getDate().toString());
            map.put("weight", e.getWeight());
            map.put("caloriesConsumed", e.getCaloriesConsumed());
            map.put("caloriesBurnt", e.getCaloriesBurnt());
            map.put("bmr", bmrService.calculateBmrForEntry(e));
            map.put("bmi", bmiCalculator.calculateBmi(e.getWeight(), user.getHeight()));
            entryData.add(map);
        }
        return entryData;
    }
}
