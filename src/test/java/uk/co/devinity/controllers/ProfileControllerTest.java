package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import uk.co.devinity.controllers.mvc.ProfileController;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.ProfileService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController controller;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("alice@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void whenProfile_thenModelContainsUser() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setName("Alice");
        user.setRoles(Set.of("ROLE_USER"));

        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(user));

        Model model = new ConcurrentModel();
        String view = controller.profile(userDetails, model);

        assertEquals("profile/user-profile", view);
        assertTrue(model.containsAttribute("user"));
        assertEquals(user, model.getAttribute("user"));
    }

    @Test
    void whenProfileUserNotFound_thenThrowsException() {
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.empty());
        Model model = new ConcurrentModel();

        assertThrows(IllegalArgumentException.class,
                () -> controller.profile(userDetails, model));
    }

    @Test
    void whenResetPasswordSuccess_thenSuccessMessageSet() {
        User user = new User();
        user.setEmail("alice@example.com");
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(user));

        Model model = new ConcurrentModel();
        String view = controller.resetPassword(
                userDetails,
                "oldPass",
                "newPass",
                "newPass",
                model
        );

        verify(profileService).updatePassword("alice@example.com", "oldPass", "newPass", "newPass");

        assertEquals("profile/user-profile", view);
        assertTrue(model.containsAttribute("successMessage"));
        assertEquals("Password updated successfully.", model.getAttribute("successMessage"));
        assertEquals(user, model.getAttribute("user"));
    }

    @Test
    void whenResetPasswordFails_thenErrorMessageSet() {
        User user = new User();
        user.setEmail("alice@example.com");
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(user));
        doThrow(new IllegalArgumentException("Current password is incorrect."))
                .when(profileService).updatePassword(any(), any(), any(), any());

        Model model = new ConcurrentModel();
        String view = controller.resetPassword(
                userDetails,
                "wrongPass",
                "newPass",
                "newPass",
                model
        );

        assertEquals("profile/user-profile", view);
        assertTrue(model.containsAttribute("errorMessage"));
        assertEquals("Current password is incorrect.", model.getAttribute("errorMessage"));
        assertEquals(user, model.getAttribute("user"));
    }
}
