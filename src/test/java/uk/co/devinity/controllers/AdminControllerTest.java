package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.WorkoutTypeService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AdminController underTest;
    private WorkoutTypeService workoutTypeService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        workoutTypeService = mock(WorkoutTypeService.class);
        passwordEncoder = new BCryptPasswordEncoder();
        underTest = new AdminController(userRepository, passwordEncoder, workoutTypeService);
    }

    @Test
    void whenCreateUser_thenPasswordIsEncodedAndDefaultRoleAdded() {
        User u = new User();
        u.setEmail("bob@example.com");
        u.setPassword("plaintext");
        u.setRoles(null);

        underTest.createUser(u);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("bob@example.com");
        assertThat(saved.getPassword()).isNotEqualTo("plaintext");
        assertThat(saved.getRoles()).contains("ROLE_USER");
    }

    @Test
    void whenNewUserForm_thenModelPrepared() {
        String view = underTest.newUserForm(mock(org.springframework.ui.Model.class));
        assertThat(view).isEqualTo("admin/new-user");
    }

    @Test
    void whenListUsers_thenUsersAddedToModel() {
        String view = underTest.listUsers(mock(org.springframework.ui.Model.class));
        assertThat(view).isEqualTo("admin/users");
    }

    @Test
    void whenActivateUser_thenUserIsActivated() {
        Long userId = 1L;
        User user = new User();
        user.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String view = underTest.activateUser(userId);

        assertThat(user.isActive()).isTrue();
        verify(userRepository, times(1)).save(user);
        assertThat(view).isEqualTo("redirect:/admin/users");
    }

    @Test
    void whenDeactivateUser_thenUserIsDeactivated() {
        Long userId = 1L;
        User user = new User();
        user.setActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String view = underTest.deactivateUser(userId);

        assertThat(user.isActive()).isFalse();
        verify(userRepository, times(1)).save(user);
        assertThat(view).isEqualTo("redirect:/admin/users");
    }

    @Test
    void whenResetPasswordForm_thenUserIsLoadedAndPasswordClearedAndAddedToModel() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setPassword("oldPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Model model = new ConcurrentModel();
        String view = underTest.resetPasswordForm(userId, model);

        assertThat(view).isEqualTo("admin/reset-password");
        assertThat(((User) model.getAttribute("user")).getId()).isEqualTo(userId);
        assertThat(((User) model.getAttribute("user")).getPassword()).isEmpty();
    }

    @Test
    void whenResetPasswordForm_withInvalidUserId_thenThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.resetPasswordForm(99L, new ConcurrentModel()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid user Id:99");
    }

    @Test
    void whenResetPassword_thenPasswordIsEncodedAndUserSaved() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPassword("oldPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        User inputUser = new User();
        inputUser.setId(userId);
        inputUser.setPassword("newPassword");

        String view = underTest.resetPassword(inputUser);

        assertThat(view).isEqualTo("redirect:/admin/users");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertThat(savedUser.getId()).isEqualTo(userId);
        assertThat(savedUser.getPassword()).isNotEqualTo("newPassword"); // must be encoded
        assertThat(passwordEncoder.matches("newPassword", savedUser.getPassword())).isTrue();
    }

    @Test
    void whenResetPassword_withInvalidUserId_thenDoesNothing() {
        User inputUser = new User();
        inputUser.setId(99L);
        inputUser.setPassword("newPassword");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String view = underTest.resetPassword(inputUser);

        assertThat(view).isEqualTo("redirect:/admin/users");
        verify(userRepository, never()).save(any());
    }
}
