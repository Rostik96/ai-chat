package dev.rost.aichat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@RequiredArgsConstructor
class ChatInteractionService {

    private final ChatRepository repo;
    private final ChatClient chatClient;
    private final TransactionTemplate tx;


    @Transactional
    Flux<String> streamAssistantReply(Long chatId, String userPrompt) {
        repo.findById(chatId).orElseThrow()
                .addEntry(MessageType.USER, userPrompt);
        var assistantText = new StringBuilder();
        return chatClient.prompt(userPrompt)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, String.valueOf(chatId)))
                .stream()
                .content()
                .doOnNext(assistantText::append)
                .doOnComplete(() -> tx.executeWithoutResult(_ -> repo.findById(chatId).orElseThrow()
                        .addEntry(MessageType.ASSISTANT, assistantText.toString())));
    }
}
