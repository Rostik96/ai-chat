package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static dev.rost.aichat.rag.DocumentSource.USER;

@Slf4j
@Service
@RequiredArgsConstructor
class UserDocumentsHousekeepingUseCase {

    @Value("${rag.user-documents.retention}")
    private final Duration userRagRetention;
    private final DocumentRepository repository;
    private final DocumentIndexingService documentIndexing;


    void cleanup() {
        var cutoff = LocalDateTime.now().minus(userRagRetention);
        var expired = repository.findBySourceAndLoadedAtBefore(USER, cutoff);
        log.info("RAG user documents housekeeping: retention={}, cutoff={}, expired={}",
                userRagRetention, cutoff, expired.size());
        if (expired.isEmpty()) return;
        for (var doc : expired) {
            documentIndexing.delete(doc.getFilename(), USER);
            repository.delete(doc);
            log.info("RAG user document expired: filename={}, loadedAt={}", doc.getFilename(), doc.getLoadedAt());
        }
    }
}
