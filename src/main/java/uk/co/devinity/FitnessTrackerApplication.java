package uk.co.devinity;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import java.util.Set;

@SpringBootApplication
public class FitnessTrackerApplication {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public FitnessTrackerApplication(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(FitnessTrackerApplication.class, args);
    }

    @PostConstruct
    public void run() throws Exception {
        String adminUsername = "admin@fitnesstracker.com";

        userRepository.findByEmailAndActiveIsTrue(adminUsername).ifPresentOrElse(
                user -> System.out.println("Admin user already exists: " + adminUsername),
                () -> {
                    User admin = new User();
                    admin.setName("Admin");
                    admin.setEmail(adminUsername);
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));
                    admin.setActive(true);
                    userRepository.save(admin);
                    System.out.println("Admin user created: " + adminUsername);

                });

    }
}
