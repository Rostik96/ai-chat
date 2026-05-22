package dev.rost.aichat.rag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

interface DocumentRepository extends JpaRepository<LoadedDocument, Long> {

    boolean existsByFilenameAndContentHashAndSource(String filename, String contentHash, DocumentSource source);

    void deleteByFilenameAndSource(String filename, DocumentSource source);

    List<LoadedDocument> findBySourceAndLoadedAtBefore(DocumentSource source, LocalDateTime loadedAt);
}
