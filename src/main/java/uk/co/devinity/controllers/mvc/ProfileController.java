package uk.co.devinity.controllers.mvc;

import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.ProfileService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final ProfileService profileService;

    public ProfileController(UserRepository userRepository, ProfileService profileService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmailAndActiveIsTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        return "profile/user-profile";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam @NotBlank String currentPassword,
                                @RequestParam @NotBlank String newPassword,
                                @RequestParam @NotBlank String confirmPassword,
                                Model model) {

        try {
            profileService.updatePassword(userDetails.getUsername(), currentPassword, newPassword, confirmPassword);
            model.addAttribute("successMessage", "Password updated successfully.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        User user = userRepository.findByEmailAndActiveIsTrue(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("bmr", profileService.getLatestBmr(user));

        return "profile/user-profile";
    }
}
