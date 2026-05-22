package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import static dev.rost.aichat.rag.DocumentSource.SYSTEM;
import static java.util.Optional.ofNullable;
import static org.springframework.util.DigestUtils.md5DigestAsHex;
import static org.springframework.util.StringUtils.getFilename;

@Slf4j
@Service
@RequiredArgsConstructor
class ClasspathKnowledgeBaseLoadingUseCase {

    private final ResourcePatternResolver resolver;
    private final DocumentReaderService documentReader;
    private final DocumentIngestionService documentIngestion;


    @SneakyThrows
    void load() {
        log.info("Loading classpath knowledge base");
        for (var resource : resolver.getResources("classpath:/knowledgebase/**/*.txt")) {
            var filename = ofNullable(getFilename(resource.getFilename()))
                    .orElseThrow();
            var contentHash = md5DigestAsHex(resource.getInputStream());
            var documents = documentReader.read(resource, filename);
            documentIngestion.ingestIfNew(filename, contentHash, documents, SYSTEM);
        }
    }
}
