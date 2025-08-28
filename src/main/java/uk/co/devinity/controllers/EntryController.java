package uk.co.devinity.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;
import uk.co.devinity.repositories.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EntryController {
    private final UserRepository userRepository;
    private final EntryRepository entryRepository;

    public EntryController(UserRepository userRepository, EntryRepository entryRepository) {
        this.userRepository = userRepository;
        this.entryRepository = entryRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "index";
    }

    @GetMapping("/entry/{userId}")
    public String entryForm(@PathVariable Long userId, Model model) {
        User user = userRepository.findById(userId).orElseThrow();
        Entry entry = new Entry();
        entry.setDate(LocalDate.now()); // prefill today
        model.addAttribute("user", user);
        model.addAttribute("entry", entry);
        return "entry-form";
    }

    @PostMapping("/entry/{userId}")
    public String submitEntry(@PathVariable Long userId, @ModelAttribute Entry entry) {
        User user = userRepository.findById(userId).orElseThrow();
        entry.setUser(user);
        entryRepository.save(entry);
        return "redirect:/combined-dashboard";
    }


    @GetMapping("/combined-dashboard")
    public String combinedDashboard(Model model) {
        List<User> users = userRepository.findAll();

        Map<Long, List<Map<String, Object>>> userEntriesMap = new HashMap<>();

        for (User user : users) {
            List<Entry> entries = entryRepository.findByUserOrderByDateAsc(user);

            List<Map<String, Object>> entryData = new ArrayList<>();
            for (Entry e : entries) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", e.getDate().toString());
                map.put("weight", e.getWeight());
                map.put("caloriesConsumed", e.getCaloriesConsumed());
                map.put("caloriesBurnt", e.getCaloriesBurnt());
                entryData.add(map);
            }
            userEntriesMap.put(user.getId(), entryData);
        }

        model.addAttribute("users", users);
        model.addAttribute("userEntriesMap", userEntriesMap);

        return "combined-dashboard";
    }

}
