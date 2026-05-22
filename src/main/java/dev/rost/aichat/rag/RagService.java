package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
class RagService {

    private final UserDocumentIngestionUseCase userDocumentIngestion;
    private final ClasspathKnowledgeBaseLoadingUseCase classpathKnowledgeBaseLoading;
    private final UserDocumentsHousekeepingUseCase userDocumentsHousekeeping;


    void ingest(MultipartFile file) {
        userDocumentIngestion.ingest(file);
    }


    @EventListener(ApplicationReadyEvent.class)
    void loadClasspathDocs() {
        classpathKnowledgeBaseLoading.load();
    }


    @Scheduled(fixedDelayString = "${rag.user-documents.cleanup-interval}")
    void housekeeping() {
        userDocumentsHousekeeping.cleanup();
    }
}
