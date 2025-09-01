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
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.Sex;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.StreamService;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ContextConfiguration(initializers = EntryControllerTestIT.Initializer.class)
@SpringBootTest(properties = {
        "spring.datasource.username=testuser",
        "spring.datasource.password=testpass",
        "spring.jpa.hibernate.ddl-auto=create-drop",
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EntryControllerTestIT {
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
    private EntryRepository entryRepository;

    @MockBean
    private StreamService streamService;

    private void createUser() {
        final var alice = new User();
        alice.setEmail("alice@example.com");
        alice.setName("Alice");
        alice.setPassword("encodedPass");
        alice.setSex(Sex.FEMALE);
        alice.setActive(true);
        alice.setHeight(165);
        alice.setWeight(60);
        alice.setRoles(Set.of("ROLE_USER"));
        userRepository.saveAndFlush(alice);
    }

    public void delete() {
        userRepository.deleteAll();
        userRepository.flush();
    }

    @WithMockUser(roles = "USER", username = "alice@example.com")
    @Test
    void whenGetEntryForm_thenModelContainsUserAndEntry() throws Exception {
        createUser();
        final var user = userRepository.findByEmailAndActiveIsTrue("alice@example.com");
        mvc.perform(get("/entry/" + user.get().getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("entry"))
                .andExpect(view().name("entry-form"));
        delete();
    }

    @WithMockUser(roles = "USER", username = "alice@example.com")
    @Test
    void whenSubmitEntry_thenSavedAndBroadcasted() throws Exception {
        createUser();
        final var user = userRepository.findByEmailAndActiveIsTrue("alice@example.com");
        mvc.perform(post("/entry/" + user.get().getId())
                        .with(csrf())
                        .param("date", LocalDate.now().toString())
                        .param("caloriesConsumed", "2000")
                        .param("caloriesBurnt", "400")
                        .param("weight", "72.2"))
                .andExpect(status().is3xxRedirection());

        verify(entryRepository).save(any(Entry.class));
        verify(streamService).broadcastNewEntry(any(Entry.class));
        delete();
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
