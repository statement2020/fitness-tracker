package uk.co.devinity.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.EntryService;
import uk.co.devinity.services.StreamService;
import uk.co.devinity.services.UserStatsServiceImpl;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntryControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private StreamService streamService;

    @Mock
    private EntryService entryService;

    @Mock
    private Model model;

    @Mock
    private UserStatsServiceImpl userStatsService;

    @InjectMocks
    private EntryController underTest;

    private Principal principal = () -> "alice@example.com";

    @Test
    void index_shouldAddUsersToModel() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        user.setActive(true);
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com"))
                .thenReturn(Optional.of(user));
        doCallRealMethod().when(userStatsService).getAllStats(any(), any());
        String view = underTest.index(model, principal);

        verify(model).addAttribute(eq("users"), any());
        assertEquals("index", view);
    }

    @Test
    void entryForm_shouldReturnEntryFormView() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String view = underTest.entryForm(1L, model);

        verify(model).addAttribute("user", user);
        verify(model).addAttribute(eq("entry"), any(Entry.class));
        assertEquals("entry-form", view);
    }

    @Test
    void submitEntry_shouldSaveEntryAndBroadcast() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Entry entry = new Entry();

        String result = underTest.submitEntry(1L, entry);

        verify(entryRepository).save(entry);
        verify(streamService).broadcastNewEntry(entry);
        assertEquals("redirect:/combined-dashboard", result);
    }

    @Test
    void combinedDashboard_shouldReturnDashboardView() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        user.setActive(true);
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com"))
                .thenReturn(Optional.of(user));
        when(entryService.getEntriesForUser(user)).thenReturn(List.of(Map.of()));

        String view = underTest.combinedDashboard(model, principal);

        verify(model).addAttribute(eq("users"), any());
        verify(model).addAttribute(eq("userEntriesMap"), any());
        assertEquals("combined-dashboard", view);
    }

    @Test
    void entries_shouldReturnEntriesView() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        user.setActive(true);
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com"))
                .thenReturn(Optional.of(user));
        when(entryService.getEntriesForUser(user)).thenReturn(List.of(Map.of()));

        String view = underTest.entries(model, principal);

        verify(model).addAttribute(eq("users"), any());
        verify(model).addAttribute(eq("userEntriesMap"), any());
        assertEquals("entries", view);
    }

    @Test
    void loadAmendEntry_shouldReturnModifyEntryView() {
        Entry entry = new Entry();
        entry.setId(1L);
        when(entryService.getEntryByIdAndUser(1L)).thenReturn(entry);
        User user = new User();
        user.setEmail("alice@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        user.setActive(true);
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(user));

        String view = underTest.loadAmendEntry(model, principal, 1L);

        verify(model).addAttribute("entry", entry);
        assertEquals("modify-entry", view);
    }

    @Test
    void amendEntry_shouldSaveEntry() {
        Entry entry = new Entry();
        entry.setId(1L);
        User user = new User();
        user.setEmail("alice@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        user.setActive(true);
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(user));

        String result = underTest.amendEntry(principal, 1L, entry);

        verify(entryRepository).save(entry);
        assertEquals("redirect:/entries", result);
    }

    @Test
    void deleteEntry_shouldDeleteById() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        user.setActive(true);
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(user));

        String result = underTest.deleteEntry(principal, 1L);

        verify(entryRepository).deleteById(1L);
        assertEquals("redirect:/entries", result);
    }
}
