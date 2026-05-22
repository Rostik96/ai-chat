package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class DocumentIngestionService {

    private final DocumentRepository repository;
    private final DocumentIndexingService documentIndexing;


    @Transactional
    void ingestIfNew(String filename, String contentHash, List<Document> documents, DocumentSource source) {
        if (repository.existsByFilenameAndContentHashAndSource(filename, contentHash, source)) {
            log.info("RAG document skipped (unchanged): filename={}, source={}", filename, source);
            return;
        }
        var chunks = documentIndexing.ingest(filename, contentHash, documents, source);
        repository.deleteByFilenameAndSource(filename, source);
        repository.save(LoadedDocument.builder()
                .documentType(Extension.of(filename).getName())
                .chunkCount(chunks.size())
                .filename(filename)
                .contentHash(contentHash)
                .source(source)
                .build());
        log.info("RAG document indexed: filename={}, source={}, contentHash={}, chunkCount={}",
                filename, source, contentHash, chunks.size());
    }
}
