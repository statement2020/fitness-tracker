package uk.co.devinity.controllers;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.EntryService;
import uk.co.devinity.services.StreamService;
import uk.co.devinity.services.UserStatsService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EntryController {
    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final StreamService streamService;
    private final EntryService entryService;
    private final UserStatsService userStatsService;

    public EntryController(UserRepository userRepository,
                           EntryRepository entryRepository,
                           StreamService streamService,
                           EntryService entryService, UserStatsService userStatsService) {
        this.userRepository = userRepository;
        this.entryRepository = entryRepository;
        this.streamService = streamService;
        this.entryService = entryService;
        this.userStatsService = userStatsService;
    }

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        List<User> users = getUsers(principal);
        userStatsService.getAllStats(model, users);
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
            var entryData = entryService.getEntriesForUser(user);
            userEntriesMap.put(user.getId(), entryData);
        }

        model.addAttribute("users", users);
        model.addAttribute("userEntriesMap", userEntriesMap);

        return "combined-dashboard";
    }

    @GetMapping("/entries")
    public String entries(Model model, Principal principal) {
        List<User> users = getUsers(principal);

        Map<Long, List<Map<String, Object>>> userEntriesMap = new HashMap<>();

        for (User user : users) {
            var entryData = entryService.getEntriesForUser(user);
            userEntriesMap.put(user.getId(), entryData);
        }

        model.addAttribute("users", users);
        model.addAttribute("userEntriesMap", userEntriesMap);
        return "entries";
    }

    @GetMapping("/entries/amend/{entryId}")
    public String loadAmendEntry(Model model, Principal principal, @PathVariable Long entryId) {
        getUsers(principal);
        var entry = entryService.getEntryByIdAndUser(entryId);
        model.addAttribute("entry", entry);
        return "modify-entry";
    }

    @PostMapping("/entries/amend/{entryId}")
    public String amendEntry(Principal principal, @PathVariable Long entryId, @ModelAttribute Entry entry) {
        getUsers(principal);
        entryRepository.save(entry);
        return "redirect:/entries";
    }

    @GetMapping("/entries/delete/{entryId}")
    public String deleteEntry(Principal principal, @PathVariable Long entryId) {
        getUsers(principal);
        entryRepository.deleteById(entryId);
        return "redirect:/entries";
    }

    private List<User> getUsers(Principal principal) {
        User currentUser = userRepository.findByEmailAndActiveIsTrue(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!currentUser.getRoles().contains("ROLE_ADMIN") && !currentUser.getEmail().equals(principal.getName())) {
            throw new AccessDeniedException("You are not allowed to view this data");
        }
        List<User> users;
        if (currentUser.getRoles().contains("ROLE_ADMIN")) {
            users = userRepository.findAllByActiveIsTrueOrderByNameAsc()
                    .stream()
                    .filter(user -> !user.getName().equals("Admin"))
                    .toList();
        } else {
            users = List.of(currentUser);
        }
        return users;
    }
}
