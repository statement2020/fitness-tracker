package uk.co.devinity.controllers;

import org.junit.jupiter.api.Test;
import uk.co.devinity.controllers.mvc.LoginController;

import static org.assertj.core.api.Assertions.assertThat;

class LoginControllerTest {

    @Test
    void whenGetLogin_thenReturnLoginView() {
        LoginController underTest = new LoginController();
        String view = underTest.login();
        assertThat(view).isEqualTo("login");
    }
}
