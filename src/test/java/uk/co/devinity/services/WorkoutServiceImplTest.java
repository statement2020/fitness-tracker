package uk.co.devinity.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.devinity.entities.WorkoutPlan;
import uk.co.devinity.entities.WorkoutType;
import uk.co.devinity.repositories.WorkoutPlanRepository;
import uk.co.devinity.repositories.WorkoutTypeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceImplTest {

    @Mock
    private WorkoutTypeRepository workoutTypeRepository;

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @InjectMocks
    private WorkoutServiceImpl underTest;

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

    @Test
    void whenGetAllWorkouts_thenDelegatesToRepository() {
        WorkoutPlan plan1 = new WorkoutPlan();
        plan1.setId(1L);
        plan1.setName("Beginner Plan");
        WorkoutPlan plan2 = new WorkoutPlan();
        plan2.setId(2L);
        plan2.setName("Advanced Plan");
        when(workoutPlanRepository.findAll()).thenReturn(List.of(plan1, plan2));

        List<WorkoutPlan> result = underTest.getAllWorkouts();

        verify(workoutPlanRepository, times(1)).findAll();
        assertThat(result).containsExactly(plan1, plan2);
    }

    @Test
    void whenSaveWorkout_thenDelegatesToRepository() {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(1L);
        plan.setName("Strength Plan");

        underTest.saveWorkout(plan);

        verify(workoutPlanRepository, times(1)).save(plan);
    }

    @Test
    void whenGetWorkoutById_thenReturnsWorkoutPlan() {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(1L);
        plan.setName("Cardio Plan");
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        WorkoutPlan result = underTest.getWorkoutById(1L);

        verify(workoutPlanRepository, times(1)).findById(1L);
        assertThat(result).isEqualTo(plan);
    }

    @Test
    void whenGetWorkoutByIdNotFound_thenReturnsNull() {
        when(workoutPlanRepository.findById(99L)).thenReturn(Optional.empty());

        WorkoutPlan result = underTest.getWorkoutById(99L);

        verify(workoutPlanRepository, times(1)).findById(99L);
        assertThat(result).isNull();
    }
}
