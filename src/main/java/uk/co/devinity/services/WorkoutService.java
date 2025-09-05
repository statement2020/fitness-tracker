package uk.co.devinity.services;

import uk.co.devinity.entities.WorkoutPlan;
import uk.co.devinity.entities.WorkoutType;

import java.util.List;

public interface WorkoutService {
    List<WorkoutType> getAllWorkoutTypes();

    void saveWorkoutType(WorkoutType workoutType);

    List<WorkoutPlan> getAllWorkouts();

    void saveWorkout(WorkoutPlan workoutPlan);

    WorkoutPlan getWorkoutById(Long id);
}
