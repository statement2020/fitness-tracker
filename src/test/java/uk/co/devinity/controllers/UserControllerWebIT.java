package uk.co.devinity.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ContextConfiguration(initializers = EntryControllerTestIT.Initializer.class)
@SpringBootTest(properties = {
        "spring.datasource.username=testuser",
        "spring.datasource.password=testpass",
        "spring.jpa.hibernate.ddl-auto=create-drop",
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerWebIT {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserDetailsService userDetailsService;

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void whenNewUserForm_thenDisplaysForm() throws Exception {
        mvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("admin/new-user"));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void whenSaveUser_thenRepositorySaveCalled() throws Exception {
        mvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("name", "Charlie")
                        .param("bmr", "1700")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository).save(org.mockito.ArgumentMatchers.any(User.class));
    }
}
