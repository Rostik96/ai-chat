package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static dev.rost.aichat.rag.DocumentIndexingService.Metadata.CONTENT_HASH;
import static dev.rost.aichat.rag.DocumentIndexingService.Metadata.FILENAME;
import static dev.rost.aichat.rag.DocumentIndexingService.Metadata.SOURCE;

@Service
@RequiredArgsConstructor
class DocumentIndexingService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter = TokenTextSplitter.builder().withChunkSize(256).build();

    List<Document> ingest(String filename, String contentHash, List<Document> documents, DocumentSource source) {
        delete(filename, source);
        documents.forEach(document ->
                document.getMetadata().putAll(Map.of(
                        FILENAME, filename,
                        SOURCE, source.name(),
                        CONTENT_HASH, contentHash)));
        var chunks = textSplitter.apply(documents);
        vectorStore.accept(chunks);
        return chunks;
    }

    void delete(String filename, DocumentSource source) {
        var filter = new FilterExpressionBuilder();
        vectorStore.delete(filter.and(
                filter.eq(FILENAME, filename),
                filter.eq(SOURCE, source.name())).build());
    }


    interface Metadata {
        String FILENAME = "filename";
        String CONTENT_HASH = "contentHash";
        String SOURCE = "source";
    }
}
