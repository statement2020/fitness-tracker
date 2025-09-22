package uk.co.devinity.controllers;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.devinity.controllers.mvc.GlobalModelAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalModelAttributesTest {

    @Mock
    private HttpSession session;

    private GlobalModelAttributes underTest = new GlobalModelAttributes();
    @Test
    void whenSessionHasRoles_thenModelAttributeReturnsRoles() {
        when(session.getAttribute("roles")).thenReturn(List.of("ROLE_USER"));

        var roles = underTest.roles(session);

        assertThat(roles).isNotNull();
        assertThat(roles).contains("ROLE_USER");
    }
}
