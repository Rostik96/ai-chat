package dev.rost.aichat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static org.zalando.fauxpas.FauxPas.throwingConsumer;

@Component
@RequiredArgsConstructor
class ChatProcessor {

    private final ChatInteractionService interaction;


    SseEmitter stream(Long chatId, String userPrompt) {
        var emitter = new SseEmitter(0L);
        interaction.streamAssistantReply(chatId, userPrompt)
                .doOnNext(throwingConsumer(token -> emitter.send(Map.of("text", token))))
                .doOnError(emitter::completeWithError)
                .doOnComplete(emitter::complete)
                .subscribe();
        return emitter;
    }
}
