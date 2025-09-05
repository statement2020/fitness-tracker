package uk.co.devinity.services;

import org.springframework.stereotype.Service;
import uk.co.devinity.entities.WorkoutPlan;
import uk.co.devinity.entities.WorkoutType;
import uk.co.devinity.repositories.WorkoutPlanRepository;
import uk.co.devinity.repositories.WorkoutTypeRepository;

import java.util.List;

@Service
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutTypeRepository workoutTypeRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    public WorkoutServiceImpl(WorkoutTypeRepository workoutTypeRepository, WorkoutPlanRepository workoutPlanRepository) {
        this.workoutTypeRepository = workoutTypeRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    @Override
    public List<WorkoutType> getAllWorkoutTypes() {
        return workoutTypeRepository.findAll();
    }

    @Override
    public void saveWorkoutType(WorkoutType workoutType) {
        workoutTypeRepository.save(workoutType);
    }

    @Override
    public List<WorkoutPlan> getAllWorkouts() {
        return workoutPlanRepository.findAll();
    }

    @Override
    public void saveWorkout(WorkoutPlan workoutPlan) {
        workoutPlanRepository.save(workoutPlan);
    }

    @Override
    public WorkoutPlan getWorkoutById(Long id) {
        return workoutPlanRepository.findById(id).orElse(null);
    }
}
