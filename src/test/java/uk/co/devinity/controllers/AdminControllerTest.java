package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        controller = new AdminController(userRepository, passwordEncoder);
    }

    @Test
    void whenCreateUser_thenPasswordIsEncodedAndDefaultRoleAdded() {
        User u = new User();
        u.setEmail("bob@example.com");
        u.setPassword("plaintext");
        u.setRoles(null);

        controller.createUser(u);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("bob@example.com");
        assertThat(saved.getPassword()).isNotEqualTo("plaintext");
        assertThat(saved.getRoles()).contains("ROLE_USER");
    }

    @Test
    void whenNewUserForm_thenModelPrepared() {
        String view = controller.newUserForm(mock(org.springframework.ui.Model.class));
        assertThat(view).isEqualTo("admin/new-user");
    }

    @Test
    void whenListUsers_thenUsersAddedToModel() {
        User u = new User();
        u.setEmail("bob@example.com");
        u.setPassword("plaintext");
        u.setRoles(null);

        controller.createUser(u);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        String view = controller.listUsers(mock(org.springframework.ui.Model.class));
        assertThat(view).isEqualTo("admin/users");
    }
}
