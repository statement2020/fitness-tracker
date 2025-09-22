package uk.co.devinity.controllers.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import uk.co.devinity.dtos.JwtResponse;
import uk.co.devinity.dtos.LoginRequest;
import uk.co.devinity.services.JwtTokenServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenServiceImpl tokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private final String mockJwt = "mock.jwt.token";

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void whenAuthenticateUser_withValidCredentials_shouldReturnJwtResponse() {
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn(mockJwt);

        JwtResponse response = authController.authenticateUser(loginRequest);

        assertThat(response).isInstanceOf(JwtResponse.class);
        assertThat(response.getToken()).isEqualTo(mockJwt);
    }

    @Test
    void whenAuthenticateUser_withInvalidCredentials_shouldThrowException() {
        LoginRequest invalidLoginRequest = new LoginRequest();
        invalidLoginRequest.setUsername("invalid@example.com");
        invalidLoginRequest.setPassword("wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(AuthenticationException.class, () -> {
            authController.authenticateUser(invalidLoginRequest);
        });
    }
}