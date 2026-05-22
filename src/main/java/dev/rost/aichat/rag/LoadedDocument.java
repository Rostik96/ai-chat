package dev.rost.aichat.rag;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class LoadedDocument {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String filename;
    private String contentHash;
    @Enumerated(STRING)
    private DocumentSource source;
    private String documentType;
    private int chunkCount;
    @CreationTimestamp
    private LocalDateTime loadedAt;
}
