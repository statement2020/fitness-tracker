package uk.co.devinity.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceImplTest {

    @Mock
    private EntryRepository entryRepository;

    @InjectMocks
    private UserStatsServiceImpl service;

    private User user;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        today = LocalDate.now();
    }

    private Entry entry(LocalDate date, double weight, double consumed, double burnt) {
        Entry e = new Entry();
        e.setDate(date);
        e.setWeight(weight);
        e.setCaloriesConsumed(consumed);
        e.setCaloriesBurnt(burnt);
        e.setUser(user);
        return e;
    }

    @Test
    void testGetWeightLossThisWeek() {
        List<Entry> entries = List.of(
                entry(today.minusDays(6), 80.0, 2000, 500),
                entry(today, 78.0, 2100, 600)
        );
        when(entryRepository.findByUserOrderByDateAsc(user)).thenReturn(entries);

        double result = service.getWeightLossThisWeek(user);

        assertEquals(2.0, result, 0.01);
    }

    @Test
    void testGetWeightLossOverall() {
        List<Entry> entries = List.of(
                entry(today.minusDays(10), 85.0, 2000, 500),
                entry(today, 80.0, 2100, 600)
        );
        when(entryRepository.findByUserOrderByDateAsc(user)).thenReturn(entries);

        double result = service.getWeightLossOverall(user);

        assertEquals(5.0, result, 0.01);
    }

    @Test
    void testGetAverageWeightLossPerWeek() {
        List<Entry> entries = List.of(
                entry(today.minusWeeks(4), 90.0, 2000, 500),
                entry(today, 80.0, 2100, 600)
        );
        when(entryRepository.findByUserOrderByDateAsc(user)).thenReturn(entries);

        double result = service.getAverageWeightLossPerWeek(user);

        assertEquals(2.5, result, 0.01); // 10kg over 4 weeks
    }

    @Test
    void testGetAverageWeightLossPerDay() {
        List<Entry> entries = List.of(
                entry(today.minusDays(10), 85.0, 2000, 500),
                entry(today, 80.0, 2100, 600)
        );
        when(entryRepository.findByUserOrderByDateAsc(user)).thenReturn(entries);

        double result = service.getAverageWeightLossPerDay(user);

        assertEquals(0.5, result, 0.01); // 5kg over 10 days
    }

    @Test
    void testGetAverageCaloriesBurntPerDay() {
        List<Entry> entries = List.of(
                entry(today.minusDays(2), 82.0, 2000, 500),
                entry(today.minusDays(1), 81.5, 2100, 600),
                entry(today, 81.0, 2200, 700)
        );
        when(entryRepository.findByUserOrderByDateAsc(user)).thenReturn(entries);

        double result = service.getAverageCaloriesBurntPerDay(user);

        assertEquals(600.0, result, 0.01);
    }

    @Test
    void testGetAverageCaloriesConsumedPerDay() {
        List<Entry> entries = List.of(
                entry(today.minusDays(2), 82.0, 2000, 500),
                entry(today.minusDays(1), 81.5, 2100, 600),
                entry(today, 81.0, 2200, 700)
        );
        when(entryRepository.findByUserOrderByDateAsc(user)).thenReturn(entries);

        double result = service.getAverageCaloriesConsumedPerDay(user);

        assertEquals(2100.0, result, 0.01);
    }

    @Test
    void testGetAllStats() {
        List<Entry> entries = List.of(
                entry(today.minusDays(6), 80.0, 2000, 500),
                entry(today, 78.0, 2100, 600)
        );
        when(entryRepository.findByUserOrderByDateAsc(user)).thenReturn(entries);

        Model model = new ConcurrentModel();
        service.getAllStats(model, List.of(user));

        assertTrue(model.containsAttribute("users"));
        assertTrue(model.containsAttribute("userStats"));

        Map<Long, Map<String, Double>> stats = (Map<Long, Map<String, Double>>) model.getAttribute("userStats");

        assertNotNull(stats);
        assertTrue(stats.containsKey(user.getId()));
        assertEquals(2.0, stats.get(user.getId()).get("weightLossThisWeek"), 0.01);
    }
}
