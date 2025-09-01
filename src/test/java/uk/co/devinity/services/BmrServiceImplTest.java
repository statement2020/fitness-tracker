package uk.co.devinity.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.devinity.entities.ActivityLevel;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.Sex;
import uk.co.devinity.entities.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BmrServiceImplTest {

    @Mock
    private User mockUser;

    @InjectMocks
    private BmrServiceImpl bmrService;

    @Test
    void calculateBmrForEntry_withMale_andActivityLevel_returnsCorrectBmr() {
        Entry entry = new Entry();
        entry.setWeight(85.0);
        entry.setUser(mockUser);

        when(mockUser.getSex()).thenReturn(Sex.MALE);
        when(mockUser.getAge()).thenReturn(30);
        when(mockUser.getHeight()).thenReturn(180.0);
        when(mockUser.getActivityLevel()).thenReturn(ActivityLevel.MODERATELY_ACTIVE);

        double result = bmrService.calculateBmrForEntry(entry);

        assertEquals(2836.5, result, 0.001);
    }

    @Test
    void calculateBmrForEntry_withFemale_andActivityLevel_returnsCorrectBmr() {
        Entry entry = new Entry();
        entry.setWeight(60.0);
        entry.setUser(mockUser);

        when(mockUser.getSex()).thenReturn(Sex.FEMALE);
        when(mockUser.getAge()).thenReturn(25);
        when(mockUser.getHeight()).thenReturn(165.0);
        when(mockUser.getActivityLevel()).thenReturn(ActivityLevel.LIGHTLY_ACTIVE);

        double result = bmrService.calculateBmrForEntry(entry);

        assertEquals(1849.71875, result, 0.001);
    }

    @Test
    void calculateBmrForEntry_withNullActivityLevel_returnsBaseBmr() {
        Entry entry = new Entry();
        entry.setWeight(90.0);
        entry.setUser(mockUser);

        when(mockUser.getSex()).thenReturn(Sex.MALE);
        when(mockUser.getAge()).thenReturn(40);
        when(mockUser.getHeight()).thenReturn(175.0);
        when(mockUser.getActivityLevel()).thenReturn(null);

        double result = bmrService.calculateBmrForEntry(entry);

        assertEquals(1798.75, result, 0.001);
    }

    @Test
    void calculateBmrForEntry_withZeroValues_returnsCorrectBmr() {
        Entry entry = new Entry();
        entry.setWeight(0.0);
        entry.setUser(mockUser);

        when(mockUser.getSex()).thenReturn(Sex.FEMALE);
        when(mockUser.getAge()).thenReturn(0);
        when(mockUser.getHeight()).thenReturn(0.0);
        when(mockUser.getActivityLevel()).thenReturn(null);

        double result = bmrService.calculateBmrForEntry(entry);

        assertEquals(-161.0, result, 0.001);
    }
}
