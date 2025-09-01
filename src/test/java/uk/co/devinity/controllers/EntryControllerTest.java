package uk.co.devinity.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.StreamService;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntryControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private StreamService streamService;

    @InjectMocks
    private EntryController underTest;

    private Principal principal = () -> "alice@example.com";

    @Test
    void whenAdminPrincipal_thenGetUsersReturnsAll() throws Exception {
        User admin = new User();
        admin.setEmail("alice@example.com");
        admin.setRoles(Set.of("ROLE_ADMIN"));
        admin.setActive(true);
        admin.setName("admin");
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findAllByActiveIsTrueOrderByNameAsc()).thenReturn(List.of(admin));

        Method m = EntryController.class.getDeclaredMethod("getUsers", Principal.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> result = (List<User>) m.invoke(underTest, principal);

        assertEquals(1, result.size());
    }

    @Test
    void whenNormalPrincipal_thenGetUsersReturnsSelf() throws Exception {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        user.setActive(true);
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(user));

        Method m = EntryController.class.getDeclaredMethod("getUsers", Principal.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> result = (List<User>) m.invoke(underTest, principal);

        assertEquals(1, result.size());
        assertEquals("alice@example.com", result.get(0).getEmail());
    }

    @Test
    void whenUserNotFound_thenGetUsersThrows() throws Exception {
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.empty());
        Method m = EntryController.class.getDeclaredMethod("getUsers", Principal.class);
        m.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> m.invoke(underTest, principal));
        assertTrue(ex.getCause() instanceof UsernameNotFoundException);
    }

    @Test
    void whenNormalUserWithDifferentEmail_thenGetUsersThrowsAccessDenied() throws Exception {
        User user = new User();
        user.setEmail("someoneelse@example.com");
        user.setRoles(Set.of("ROLE_USER"));
        when(userRepository.findByEmailAndActiveIsTrue("alice@example.com")).thenReturn(Optional.of(user));

        Method m = EntryController.class.getDeclaredMethod("getUsers", Principal.class);
        m.setAccessible(true);
        Exception ex = assertThrows(Exception.class, () -> m.invoke(underTest, principal));
        assertTrue(ex.getCause() instanceof AccessDeniedException);
    }
}
