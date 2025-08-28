package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.Model;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserRepository userRepository;
    private UserController userController;
    private Model model;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userController = new UserController(userRepository);
        model = mock(Model.class);
    }

    @Test
    void newUserForm_ShouldAddEmptyUserToModel_AndReturnForm() {
        String viewName = userController.newUserForm(model);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(model).addAttribute(eq("user"), captor.capture());

        assertThat(captor.getValue()).isInstanceOf(User.class);
        assertThat(viewName).isEqualTo("user-form");
    }

    @Test
    void saveUser_ShouldSaveUser_AndRedirectToIndex() {
        User user = new User("Alice", 1500);

        String viewName = userController.saveUser(user);

        verify(userRepository).save(user);
        assertThat(viewName).isEqualTo("redirect:/");
    }
}
