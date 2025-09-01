package uk.co.devinity.services;

import uk.co.devinity.entities.User;

public interface ProfileService {
    void updatePassword(String email, String currentPassword, String newPassword, String confirmPassword);

    double getLatestBmr(User user);
}
