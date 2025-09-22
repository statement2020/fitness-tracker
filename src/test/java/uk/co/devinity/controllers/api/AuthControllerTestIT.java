package uk.co.devinity.controllers.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import uk.co.devinity.dtos.ErrorResponse;
import uk.co.devinity.dtos.JwtResponse;
import uk.co.devinity.dtos.LoginRequest;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(initializers = AuthControllerTestIT.Initializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.username=testuser",
        "spring.datasource.password=testpass",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthControllerTestIT {

    @Container
    static PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("fitnesstracker")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        User testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setName("Test User");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoles(java.util.Set.of("ROLE_USER"));
        testUser.setActive(true);
        userRepository.save(testUser);
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnsJwtToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser@example.com");
        loginRequest.setPassword("password123");

        ResponseEntity<JwtResponse> response = restTemplate.postForEntity("/api/auth/login", loginRequest, JwtResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotNull().isNotEmpty();
    }

    @Test
    void whenLoginWithInvalidCredentials_thenReturnsUnauthorized() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser@example.com");
        loginRequest.setPassword("wrongpassword");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/auth/login", loginRequest, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            POSTGRES_SQL_CONTAINER.start();
            TestPropertyValues.of("spring.datasource.url=" + POSTGRES_SQL_CONTAINER.getJdbcUrl())
                    .applyTo(context.getEnvironment());
        }
    }
}