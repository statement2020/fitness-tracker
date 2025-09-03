package uk.co.devinity.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Table(name = "entries")
@Entity
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private double caloriesConsumed;
    private double caloriesBurnt;
    private double weight;

    @ManyToOne
    private User user;

    public Entry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getCaloriesConsumed() {
        return caloriesConsumed;
    }

    public void setCaloriesConsumed(double caloriesConsumed) {
        this.caloriesConsumed = caloriesConsumed;
    }

    public double getCaloriesBurnt() {
        return caloriesBurnt;
    }

    public void setCaloriesBurnt(double caloriesBurnt) {
        this.caloriesBurnt = caloriesBurnt;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Entry{");
        sb.append("id=").append(id);
        sb.append(", date=").append(date);
        sb.append(", caloriesConsumed=").append(caloriesConsumed);
        sb.append(", caloriesBurnt=").append(caloriesBurnt);
        sb.append(", weight=").append(weight);
        sb.append(", user=").append(user);
        sb.append('}');
        return sb.toString();
    }
}
