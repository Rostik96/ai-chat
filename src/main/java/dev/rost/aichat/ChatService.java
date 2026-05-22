package dev.rost.aichat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class ChatService {

    private final ChatRepository repo;
    private final ChatMemory chatMemory;
    private final ChatViewMapper chatViewMapper;


    ChatView create(String title) {
        return chatViewMapper.from(repo.save(ChatEntity.builder()
                .title(title)
                .build()));
    }

    @Transactional(readOnly = true)
    List<ChatView> getAll() {
        return repo.findAllByOrderByCreatedAtDesc().stream()
                .map(chatViewMapper::from)
                .toList();
    }

    @Transactional(readOnly = true)
    ChatView get(Long chatId) {
        return chatViewMapper.from(
                repo.findById(chatId).orElseThrow());
    }

    @Transactional
    void delete(Long chatId) {
        repo.deleteById(chatId);
        chatMemory.clear(String.valueOf(chatId));
    }
}
