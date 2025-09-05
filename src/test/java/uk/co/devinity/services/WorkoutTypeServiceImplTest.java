package uk.co.devinity.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.devinity.entities.WorkoutType;
import uk.co.devinity.repositories.WorkoutTypeRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutTypeServiceImplTest {

    @Mock
    private WorkoutTypeRepository workoutTypeRepository;

    @InjectMocks
    private WorkoutTypeServiceImpl underTest;

    @Test
    void whenGetAllWorkoutTypes_thenDelegatesToRepository() {
        WorkoutType wt1 = new WorkoutType();
        wt1.setId(1L);
        wt1.setWorkoutName("Cardio");
        WorkoutType wt2 = new WorkoutType();
        wt2.setId(2L);
        wt2.setWorkoutName("Strength");
        when(workoutTypeRepository.findAll()).thenReturn(List.of(wt1, wt2));

        List<WorkoutType> result = underTest.getAllWorkoutTypes();

        verify(workoutTypeRepository, times(1)).findAll();
        assertThat(result).containsExactly(wt1, wt2);
    }

    @Test
    void whenSaveWorkoutType_thenDelegatesToRepository() {
        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);
        workoutType.setWorkoutName("Yoga");

        underTest.saveWorkoutType(workoutType);

        verify(workoutTypeRepository, times(1)).save(workoutType);
    }
}
