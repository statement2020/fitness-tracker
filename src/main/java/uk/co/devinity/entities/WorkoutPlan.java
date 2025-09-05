package uk.co.devinity.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_plans")
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int duration; // in minutes

    @ManyToMany
    @JoinTable(
            name = "workout_plan_types",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private List<WorkoutType> workoutTypes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<WorkoutType> getWorkoutTypes() {
        return workoutTypes;
    }

    public void setWorkoutTypes(List<WorkoutType> workoutTypes) {
        this.workoutTypes = workoutTypes;
    }

    public void addWorkoutType(WorkoutType workoutType) {
        this.workoutTypes.add(workoutType);
    }

    public void removeWorkoutType(WorkoutType workoutType) {
        this.workoutTypes.remove(workoutType);
    }
}
