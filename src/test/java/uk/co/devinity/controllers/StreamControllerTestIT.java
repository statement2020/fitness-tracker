package uk.co.devinity.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uk.co.devinity.repositories.UserRepository;
import uk.co.devinity.services.StreamService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StreamController.class)
class StreamControllerTestIT {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StreamService streamService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void whenStreamEntries_thenReturnsSseEmitter() throws Exception {
        SseEmitter emitter = new SseEmitter();
        when(streamService.streamEntries()).thenReturn(emitter);

        mvc.perform(get("/stream/entries"))
                .andExpect(status().isOk());
    }
}
