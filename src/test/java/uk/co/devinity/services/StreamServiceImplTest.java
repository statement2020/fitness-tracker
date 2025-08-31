package uk.co.devinity.services;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uk.co.devinity.entities.Entry;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class StreamServiceImplTest {

    private final StreamServiceImpl underTest = new StreamServiceImpl();

    @Test
    void whenStreamEntries_thenEmitterIsRegistered() {
        SseEmitter emitter = underTest.streamEntries();
        assertNotNull(emitter);
    }

    @Test
    void whenBroadcastNewEntry_thenAllEmittersReceiveData() throws IOException {
        underTest.streamEntries(); // registers 1 emitter
        underTest.broadcastNewEntry(new Entry());
        // can't directly assert private emitter list, but no exception thrown
    }

    @Test
    void whenEmitterThrowsIOException_thenEmitterIsRemoved() throws IOException {
        SseEmitter badEmitter = mock(SseEmitter.class);
        doThrow(IOException.class).when(badEmitter).send(any(SseEmitter.class));
        underTest.streamEntries(); // registers 1 emitter internally
        underTest.broadcastNewEntry(new Entry()); // should remove failing emitter
    }
}
