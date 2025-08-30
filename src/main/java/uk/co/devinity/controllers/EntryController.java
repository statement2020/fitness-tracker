package uk.co.devinity.controllers;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import uk.co.devinity.services.StreamService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class EntryController {
    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final StreamService streamService;

    public EntryController(UserRepository userRepository, EntryRepository entryRepository, StreamService streamService) {
        this.userRepository = userRepository;
        this.entryRepository = entryRepository;
        this.streamService = streamService;
    }

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        List<User> users = getUsers(principal);
        model.addAttribute("users", users);
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
        streamService.broadcastNewEntry(entry);
        return "redirect:/combined-dashboard";
    }


    @GetMapping("/combined-dashboard")
    public String combinedDashboard(Model model, Principal principal) {
        List<User> users = getUsers(principal);

        Map<Long, List<Map<String, Object>>> userEntriesMap = new HashMap<>();

        for (User user : users) {
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
                entryData.add(map);
            }
            userEntriesMap.put(user.getId(), entryData);
        }

        model.addAttribute("users", users);
        model.addAttribute("userEntriesMap", userEntriesMap);

        return "combined-dashboard";
    }

    private List<User> getUsers(Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!currentUser.getRoles().contains("ROLE_ADMIN") && !currentUser.getEmail().equals(principal.getName())) {
            throw new AccessDeniedException("You are not allowed to view this data");
        }
        List<User> users;
        if (currentUser.getRoles().contains("ROLE_ADMIN")) {
            users = userRepository.findAll();
        } else {
            users = List.of(currentUser);
        }
        return users;
    }
}
