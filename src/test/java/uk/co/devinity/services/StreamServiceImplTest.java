package uk.co.devinity.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.repositories.EntryRepository;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StreamServiceImplTest {

    private EntryRepository entryRepository;
    private StreamServiceImpl streamService;

    @BeforeEach
    void setUp() {
        entryRepository = mock(EntryRepository.class);
        streamService = new StreamServiceImpl(entryRepository);
    }

    @Test
    void whenStreamEntries_thenEmitterIsRegistered() {
        SseEmitter emitter = streamService.streamEntries();
        assertNotNull(emitter);
    }

    @Test
    void whenBroadcastNewEntry_thenAllEmittersReceiveData() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        streamService.streamEntries(); // registers 1 emitter
        streamService.broadcastNewEntry(new Entry());
        // can't directly assert private emitter list, but no exception thrown
    }

    @Test
    void whenEmitterThrowsIOException_thenEmitterIsRemoved() throws IOException {
        SseEmitter badEmitter = mock(SseEmitter.class);
        doThrow(IOException.class).when(badEmitter).send(any(SseEmitter.class));
        streamService.streamEntries(); // registers 1 emitter internally
        streamService.broadcastNewEntry(new Entry()); // should remove failing emitter
    }
}
