package dev.rost.aichat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "chat")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
class ChatEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String title;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OrderBy("createdAt")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "chat_id", nullable = false)
    @Builder.Default
    private List<ChatEntryEntity> entries = new ArrayList<>();

    void addEntry(MessageType role, String content) {
        entries.add(new ChatEntryEntity(role.getValue(), content));
    }
}
