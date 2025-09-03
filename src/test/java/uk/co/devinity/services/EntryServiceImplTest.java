package uk.co.devinity.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntryServiceImplTest {

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private BmrService bmrService;

    @Mock
    private BmiCalculator bmiCalculator;

    @InjectMocks
    private EntryServiceImpl entryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
    }

    private Entry createEntry(LocalDate date, double weight) {
        Entry entry = new Entry();
        entry.setDate(date);
        entry.setWeight(weight);
        entry.setCaloriesConsumed(2000);
        entry.setCaloriesBurnt(300);
        return entry;
    }

    @Test
    void getEntriesForUser_returnsCorrectData_withUniqueDates() {
        Entry entry2 = createEntry(LocalDate.of(2023, 10, 2), 75.5);
        Entry entry1 = createEntry(LocalDate.of(2023, 10, 1), 76.0);
        Entry entry3 = createEntry(LocalDate.of(2023, 10, 3), 75.0);
        List<Entry> mockEntries = List.of(entry2, entry1, entry3);

        when(entryRepository.findByUserOrderByDateAsc(testUser)).thenReturn(mockEntries);
        when(bmrService.calculateBmrForEntry(any(Entry.class))).thenReturn(2000.0);
        when(bmiCalculator.calculateBmi(anyDouble(), anyDouble())).thenReturn(22.0);
        List<Map<String, Object>> result = entryService.getEntriesForUser(testUser);

        assertEquals(3, result.size(), "Should return three entries.");

        Map<String, Object> firstResult = result.get(0);
        assertEquals("2023-10-01", firstResult.get("date"));
        assertEquals(76.0, firstResult.get("weight"));
        assertEquals(2000.0, firstResult.get("bmr"));

        Map<String, Object> secondResult = result.get(1);
        assertEquals("2023-10-02", secondResult.get("date"));
        assertEquals(75.5, secondResult.get("weight"));
        assertEquals(2000.0, secondResult.get("bmr"));

        Map<String, Object> thirdResult = result.get(2);
        assertEquals("2023-10-03", thirdResult.get("date"));
        assertEquals(75.0, thirdResult.get("weight"));
        assertEquals(2000.0, thirdResult.get("bmr"));

        // Verify BmrService was called for each entry
        verify(bmrService, times(3)).calculateBmrForEntry(any(Entry.class));
    }

    @Test
    void getEntriesForUser_handlesDuplicateDates_keepsReplacement() {
        LocalDate duplicateDate = LocalDate.of(2023, 10, 5);
        Entry entry1 = createEntry(duplicateDate, 80.0);
        Entry entry2 = createEntry(duplicateDate, 79.5); // This one should be kept
        List<Entry> mockEntries = List.of(entry1, entry2);

        when(entryRepository.findByUserOrderByDateAsc(testUser)).thenReturn(mockEntries);
        when(bmrService.calculateBmrForEntry(any(Entry.class))).thenReturn(2000.0);
        when(bmiCalculator.calculateBmi(anyDouble(), anyDouble())).thenReturn(22.0);
        List<Map<String, Object>> result = entryService.getEntriesForUser(testUser);

        assertEquals(1, result.size(), "Should return only one entry for the duplicate date.");
        Map<String, Object> singleResult = result.get(0);
        assertEquals("2023-10-05", singleResult.get("date"));
        assertEquals(79.5, singleResult.get("weight"), "The latest entry's weight should be kept.");
        assertEquals(2000.0, singleResult.get("bmr"));

        verify(bmrService, times(1)).calculateBmrForEntry(entry2);
    }

    @Test
    void getEntriesForUser_returnsEmptyList_whenRepositoryIsEmpty() {
        when(entryRepository.findByUserOrderByDateAsc(testUser)).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = entryService.getEntriesForUser(testUser);

        assertTrue(result.isEmpty(), "The result list should be empty.");

        verifyNoInteractions(bmrService);
    }
}
