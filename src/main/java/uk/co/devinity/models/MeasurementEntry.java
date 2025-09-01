package uk.co.devinity.models;

import uk.co.devinity.entities.User;

import java.time.LocalDate;

public class MeasurementEntry {

    private Long id;

    private User user;

    private LocalDate date;
    private double weight;
    private double caloriesConsumed;
    private double caloriesBurnt;
    private double bmr;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
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

    public double getBmr() {
        return bmr;
    }

    public void setBmr(double bmr) {
        this.bmr = bmr;
    }
}
