package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AdminController underTest;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        underTest = new AdminController(userRepository, passwordEncoder);
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
}
