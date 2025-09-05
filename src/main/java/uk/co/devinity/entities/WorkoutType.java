package uk.co.devinity.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "workout_types")
public class WorkoutType {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String workoutName;

    private String description;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private FunctionalType functionalType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FunctionalType getFunctionalType() {
        return functionalType;
    }

    public void setFunctionalType(FunctionalType functionalType) {
        this.functionalType = functionalType;
    }
}
