package uk.co.devinity.services;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uk.co.devinity.entities.Entry;

public interface StreamService {

    SseEmitter streamEntries();

    void broadcastNewEntry(Entry entry);
}
