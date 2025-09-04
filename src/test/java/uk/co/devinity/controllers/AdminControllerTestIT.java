package uk.co.devinity.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import uk.co.devinity.entities.ActivityLevel;
import uk.co.devinity.entities.Sex;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ContextConfiguration(initializers = AdminControllerTestIT.Initializer.class)
@SpringBootTest(properties = {
        "spring.datasource.username=testuser",
        "spring.datasource.password=testpass",
        "spring.jpa.hibernate.ddl-auto=create-drop"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AdminControllerTestIT {

    @Container
    static PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("fitnesstracker")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;

    @BeforeEach
    void setup() {
        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setName("Admin User");
        adminUser.setPassword("pass");
        adminUser.setRoles(java.util.Set.of("ROLE_ADMIN"));
        adminUser.setActive(true);
        userRepository.save(adminUser);
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @Test
    void whenListUsers_thenReturnsCorrectViewAndUsersInModel() throws Exception {
        mvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));
    }

    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @Test
    void whenNewUserForm_thenReturnsCorrectViewAndAttributesInModel() throws Exception {
        mvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/new-user"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allRoles"));
    }

    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @Test
    void whenCreateUser_thenUserIsSavedAndRedirects() throws Exception {
        mvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("name", "Test User")
                        .param("email", "test@example.com")
                        .param("password", "plaintextpass")
                        .param("weight", "70.5")
                        .param("height", "175.0")
                        .param("dateOfBirth", "1990-01-01")
                        .param("sex", Sex.MALE.toString())
                        .param("activityLevel", ActivityLevel.SEDENTARY.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/users"));

        User savedUser = userRepository.findByEmailAndActiveIsTrue("test@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getRoles()).contains("ROLE_USER");
    }

    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @Test
    void whenActivateUser_thenUserIsActivatedAndRedirects() throws Exception {
        User userToActivate = new User();
        userToActivate.setEmail("inactive@example.com");
        userToActivate.setName("Inactive User");
        userToActivate.setPassword("pass");
        userToActivate.setActive(false);
        userRepository.save(userToActivate);

        mvc.perform(post("/admin/users/" + userToActivate.getId() + "/activate")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/users"));

        User activatedUser = userRepository.findById(userToActivate.getId()).orElseThrow();
        assertThat(activatedUser.isActive()).isTrue();
    }

    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @Test
    void whenDeactivateUser_thenUserIsDeactivatedAndRedirects() throws Exception {
        User userToDeactivate = new User();
        userToDeactivate.setEmail("active@example.com");
        userToDeactivate.setName("Active User");
        userToDeactivate.setPassword("pass");
        userToDeactivate.setActive(true);
        userRepository.save(userToDeactivate);

        mvc.perform(post("/admin/users/" + userToDeactivate.getId() + "/deactivate")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/users"));

        User deactivatedUser = userRepository.findById(userToDeactivate.getId()).orElseThrow();
        assertThat(deactivatedUser.isActive()).isFalse();
    }

    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @Test
    void whenResetPasswordForm_thenReturnsCorrectViewAndModel() throws Exception {
        User user = new User();
        user.setEmail("reset@example.com");
        user.setName("Reset User");
        user.setPassword("secret");
        user.setActive(true);
        userRepository.save(user);

        mvc.perform(get("/admin/users/" + user.getId() + "/reset-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reset-password"))
                .andExpect(model().attributeExists("user"));

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getPassword()).isEqualTo("secret");
    }

    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @Test
    void whenResetPassword_thenPasswordIsUpdatedAndRedirects() throws Exception {
        User user = new User();
        user.setEmail("changepass@example.com");
        user.setName("Change Pass User");
        user.setPassword("oldpassword");
        user.setActive(true);
        userRepository.save(user);

        mvc.perform(post("/admin/users/" + user.getId() + "/reset-password")
                        .with(csrf())
                        .param("id", user.getId().toString())
                        .param("password", "newStrongPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/users"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getPassword()).isNotEqualTo("newStrongPassword123");
        assertThat(updated.getPassword()).startsWith("$2a$"); // BCrypt hash prefix
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
