package dev.rost.aichat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
class ChatController {

    private final ChatProcessor chatProcessor;


    @GetMapping(value = "/{id:\\d+}/stream", produces = TEXT_EVENT_STREAM_VALUE)
    SseEmitter chatCompletions(@PathVariable Long id, @RequestParam String prompt) {
        return chatProcessor.stream(id, prompt);
    }
}
