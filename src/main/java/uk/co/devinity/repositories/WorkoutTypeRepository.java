package uk.co.devinity.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.devinity.entities.WorkoutType;

@Repository
public interface WorkoutTypeRepository extends JpaRepository<WorkoutType, Long> {
}
