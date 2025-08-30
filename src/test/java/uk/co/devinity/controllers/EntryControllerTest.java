package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.StreamService;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntryControllerTest {



    private UserRepository userRepository;
    private EntryRepository entryRepository;
    private StreamService streamService;
    private EntryController controller;
    private Principal principal;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        entryRepository = mock(EntryRepository.class);
        streamService = mock(StreamService.class);
        controller = new EntryController(userRepository, entryRepository, streamService);
        principal = () -> "alice@example.com";
    }

    @Test
    void whenAdminPrincipal_thenGetUsersReturnsAll() throws Exception {
        User admin = new User();
        admin.setEmail("alice@example.com");
        admin.setRoles(Set.of("ROLE_ADMIN"));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findAll()).thenReturn(List.of(admin));

        Method m = EntryController.class.getDeclaredMethod("getUsers", Principal.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> result = (List<User>) m.invoke(controller, principal);

        assertEquals(1, result.size());
    }

    @Test
    void whenNormalPrincipal_thenGetUsersReturnsSelf() throws Exception {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Method m = EntryController.class.getDeclaredMethod("getUsers", Principal.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> result = (List<User>) m.invoke(controller, principal);

        assertEquals(1, result.size());
        assertEquals("alice@example.com", result.get(0).getEmail());
    }

    @Test
    void whenUserNotFound_thenGetUsersThrows() throws Exception {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        Method m = EntryController.class.getDeclaredMethod("getUsers", Principal.class);
        m.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> m.invoke(controller, principal));
        assertTrue(ex.getCause() instanceof UsernameNotFoundException);
    }

    @Test
    void whenNormalUserWithDifferentEmail_thenGetUsersThrowsAccessDenied() throws Exception {
        User user = new User();
        user.setEmail("someoneelse@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Method m = EntryController.class.getDeclaredMethod("getUsers", Principal.class);
        m.setAccessible(true);
        Exception ex = assertThrows(Exception.class, () -> m.invoke(controller, principal));
        assertTrue(ex.getCause() instanceof AccessDeniedException);
    }
}
