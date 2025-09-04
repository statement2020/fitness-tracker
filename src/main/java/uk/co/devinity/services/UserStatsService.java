package uk.co.devinity.services;

import org.springframework.ui.Model;
import uk.co.devinity.entities.User;

import java.util.List;

public interface UserStatsService {
    double getWeightLossThisWeek(User user);
    double getWeightLossOverall(User user);
    double getAverageWeightLossPerWeek(User user);
    double getAverageWeightLossPerDay(User user);
    double getAverageCaloriesBurntPerDay(User user);
    double getAverageCaloriesConsumedPerDay(User user);

    void getAllStats(Model model, List<User> users);
}
