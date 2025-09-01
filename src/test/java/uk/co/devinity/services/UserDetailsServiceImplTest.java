package uk.co.devinity.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl underTest;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        underTest = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void whenUserExists_thenReturnUserDetails() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPwd");
        user.setRoles(Set.of("ROLE_USER"));

        when(userRepository.findByEmailAndActiveIsTrue("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = underTest.loadUserByUsername("test@example.com");

        assertEquals("test@example.com", details.getUsername());
        assertEquals("encodedPwd", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void whenUserDoesNotExist_thenThrowException() {
        when(userRepository.findByEmailAndActiveIsTrue("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> underTest.loadUserByUsername("missing@example.com"));
    }
}
