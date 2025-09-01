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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.security.test.context.support.WithMockUser;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.entities.Sex;
import uk.co.devinity.entities.ActivityLevel;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ContextConfiguration(initializers = RegisterControllerTestIT.Initializer.class)
@SpringBootTest(properties = {
        "spring.datasource.username=testuser",
        "spring.datasource.password=testpass",
        "spring.jpa.hibernate.ddl-auto=create-drop"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RegisterControllerTestIT {

    @Container
    static PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("fitnesstracker")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void whenGetRegistrationForm_thenReturnsCorrectViewAndAttributesInModel() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void whenRegisterUser_thenUserIsSavedAndRedirects() throws Exception {
        mvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "New User")
                        .param("email", "newuser@example.com")
                        .param("password", "plaintextpass")
                        .param("weight", "65.5")
                        .param("height", "180.0")
                        .param("dateOfBirth", "1995-05-10")
                        .param("sex", Sex.FEMALE.toString())
                        .param("activityLevel", ActivityLevel.LIGHTLY_ACTIVE.toString()))
                .andExpect(status().is3xxRedirection());

        Optional<User> savedUserOpt = userRepository.findByEmail("newuser@example.com");
        assertThat(savedUserOpt).isPresent();
        User savedUser = savedUserOpt.get();
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getRoles()).contains("ROLE_USER");
        assertThat(savedUser.isActive()).isFalse();
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
