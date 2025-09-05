package uk.co.devinity.services;

import org.springframework.stereotype.Service;
import uk.co.devinity.entities.WorkoutType;
import uk.co.devinity.repositories.WorkoutTypeRepository;

import java.util.List;

@Service
public class WorkoutTypeServiceImpl implements WorkoutTypeService {

    private final WorkoutTypeRepository workoutTypeRepository;

    public WorkoutTypeServiceImpl(WorkoutTypeRepository workoutTypeRepository) {
        this.workoutTypeRepository = workoutTypeRepository;
    }

    @Override
    public List<WorkoutType> getAllWorkoutTypes() {
        return workoutTypeRepository.findAll();
    }

    @Override
    public void saveWorkoutType(WorkoutType workoutType) {
        workoutTypeRepository.save(workoutType);
    }
}
