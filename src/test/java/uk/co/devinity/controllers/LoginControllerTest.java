package uk.co.devinity.controllers;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LoginControllerTest {

    @Test
    void whenGetLogin_thenReturnLoginView() {
        LoginController c = new LoginController();
        String view = c.login();
        assertThat(view).isEqualTo("login");
    }
}
