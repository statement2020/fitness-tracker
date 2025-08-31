package uk.co.devinity.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.ProfileService;

import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ContextConfiguration(initializers = ProfileControllerTestIT.Initializer.class)
@SpringBootTest(properties = {
        "spring.datasource.username=testuser",
        "spring.datasource.password=testpass",
        "spring.jpa.hibernate.ddl-auto=create-drop",
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProfileControllerTestIT {

    @Container
    static PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("fitnesstracker")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ProfileService profileService;

    private void createUser() {
        final var alice = new User();
        alice.setEmail("alice@example.com");
        alice.setName("Alice");
        alice.setPassword("encodedPass");
        alice.setBmr(1500);
        alice.setRoles(Set.of("ROLE_USER"));
        userRepository.saveAndFlush(alice);
    }

    private void deleteUsers() {
        userRepository.deleteAll();
        userRepository.flush();
    }

    @WithMockUser(roles = "USER", username = "alice@example.com")
    @Test
    void whenGetProfile_thenModelContainsUser() throws Exception {
        createUser();

        mvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("profile/user-profile"));

        deleteUsers();
    }

    @WithMockUser(roles = "USER", username = "alice@example.com")
    @Test
    void whenResetPasswordSuccess_thenSuccessMessageInModel() throws Exception {
        createUser();

        mvc.perform(post("/profile/reset-password")
                        .with(csrf())
                        .param("currentPassword", "oldPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("successMessage", "Password updated successfully."))
                .andExpect(view().name("profile/user-profile"));

        verify(profileService).updatePassword("alice@example.com", "oldPass", "newPass", "newPass");
        deleteUsers();
    }

    @WithMockUser(roles = "USER", username = "alice@example.com")
    @Test
    void whenResetPasswordFails_thenErrorMessageInModel() throws Exception {
        createUser();
        doThrow(new IllegalArgumentException("Current password is incorrect."))
                .when(profileService).updatePassword(any(), any(), any(), any());

        mvc.perform(post("/profile/reset-password")
                        .with(csrf())
                        .param("currentPassword", "wrongPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("errorMessage", "Current password is incorrect."))
                .andExpect(view().name("profile/user-profile"));

        verify(profileService).updatePassword("alice@example.com", "wrongPass", "newPass", "newPass");
        deleteUsers();
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
