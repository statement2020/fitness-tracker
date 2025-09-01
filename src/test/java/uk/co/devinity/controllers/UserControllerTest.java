package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController underTest;

    @Mock
    private Model model;

    @Test
    void newUserForm_ShouldAddEmptyUserToModel_AndReturnForm() {
        String viewName = underTest.newUserForm(model);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(model).addAttribute(eq("user"), captor.capture());

        assertThat(captor.getValue()).isInstanceOf(User.class);
        assertThat(viewName).isEqualTo("user-form");
    }

    @Test
    void saveUser_ShouldSaveUser_AndRedirectToIndex() {
        User user = new User();
        user.setName("Alice");

        String viewName = underTest.saveUser(user);

        verify(userRepository).save(user);
        assertThat(viewName).isEqualTo("redirect:/");
    }
}
