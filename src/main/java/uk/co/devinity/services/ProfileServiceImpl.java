package uk.co.devinity.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;
import uk.co.devinity.repositories.UserRepository;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntryRepository entryRepository;
    private final BmrService bmrService;

    public ProfileServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EntryRepository entryRepository, BmrService bmrService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.entryRepository = entryRepository;
        this.bmrService = bmrService;
    }

    @Override
    public void updatePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByEmailAndActiveIsTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public double getLatestBmr(User user) {
        return entryRepository.findByUserOrderByDateAsc(user).stream().findFirst()
                .map(bmrService::calculateBmrForEntry)
                .orElse(0.0);

    }
}
