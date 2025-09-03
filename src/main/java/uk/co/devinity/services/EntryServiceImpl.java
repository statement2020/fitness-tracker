package uk.co.devinity.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EntryServiceImpl implements EntryService {

    private static final Logger LOG = LoggerFactory.getLogger(EntryServiceImpl.class);

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
            entryData.add(toSimpleMap(e));
        }
        return entryData;
    }

    @Override
    public Entry getEntryByIdAndUser(Long entryId) {
        return entryRepository.findById(entryId).orElseThrow(() -> new IllegalArgumentException("Entry not found"));
    }

    private Map<String, Object> toSimpleMap(Entry entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", entry.getDate().toString());
        map.put("weight", entry.getWeight());
        map.put("caloriesConsumed", entry.getCaloriesConsumed());
        map.put("caloriesBurnt", entry.getCaloriesBurnt());
        map.put("bmr", bmrService.calculateBmrForEntry(entry));
        map.put("bmi", bmiCalculator.calculateBmi(entry.getWeight(), entry.getUser().getHeight()));
        map.put("id", entry.getId());
        return map;
    }
}
