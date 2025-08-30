package uk.co.devinity.controllers;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalModelAttributesTest {

    @Test
    void whenSessionHasRoles_thenModelAttributeReturnsRoles() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("roles")).thenReturn(List.of("ROLE_USER"));

        GlobalModelAttributes g = new GlobalModelAttributes();
        var roles = g.roles(session);

        assertThat(roles).isNotNull();
        assertThat(roles).contains("ROLE_USER");
    }
}
