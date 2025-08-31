package uk.co.devinity.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileServiceImpl underTest;


    @Test
    void updatePassword_successfulUpdate() {
        String email = "test@example.com";
        String currentPassword = "oldPass";
        String newPassword = "newPass";
        String encodedNewPassword = "encodedNewPass";

        User user = new User();
        user.setEmail(email);
        user.setPassword("encodedOldPass");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        underTest.updatePassword(email, currentPassword, newPassword, newPassword);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals(encodedNewPassword, savedUser.getPassword());
    }

    @Test
    void updatePassword_userNotFound_throwsException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> underTest.updatePassword("missing@example.com", "pass", "new", "new"));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_incorrectCurrentPassword_throwsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedOldPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedOldPass")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> underTest.updatePassword("test@example.com", "wrongPass", "new", "new"));

        assertEquals("Current password is incorrect.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_passwordsDoNotMatch_throwsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedOldPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> underTest.updatePassword("test@example.com", "oldPass", "new", "different"));

        assertEquals("New password and confirmation do not match.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
