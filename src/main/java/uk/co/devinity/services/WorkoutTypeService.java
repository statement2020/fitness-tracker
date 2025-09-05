package uk.co.devinity.services;

import uk.co.devinity.entities.WorkoutType;

import java.util.List;

public interface WorkoutTypeService {
    List<WorkoutType> getAllWorkoutTypes();

    void saveWorkoutType(WorkoutType workoutType);
}
