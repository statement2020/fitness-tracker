package uk.co.devinity.services;

public interface ProfileService {
    void updatePassword(String email, String currentPassword, String newPassword, String confirmPassword);
}
