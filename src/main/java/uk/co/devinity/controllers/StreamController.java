package uk.co.devinity.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uk.co.devinity.services.StreamService;

@RestController
@RequestMapping("/stream")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    @GetMapping(value = "/entries", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEntries() {
        return streamService.streamEntries();
    }
}
