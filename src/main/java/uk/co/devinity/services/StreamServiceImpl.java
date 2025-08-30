package uk.co.devinity.services;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.repositories.EntryRepository;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StreamServiceImpl implements StreamService {
    private final List<SseEmitter> emitters;
    private final EntryRepository entryRepository;

    public StreamServiceImpl(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
        emitters = new CopyOnWriteArrayList<>();
    }

    @Override
    public SseEmitter streamEntries() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    @Override
    public void broadcastNewEntry(Entry entry) {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-entry")
                        .data(entry));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }
}
