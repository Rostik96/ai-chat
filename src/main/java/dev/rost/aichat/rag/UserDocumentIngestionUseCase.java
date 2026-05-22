package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static dev.rost.aichat.rag.DocumentSource.USER;
import static java.util.Optional.ofNullable;
import static org.springframework.util.DigestUtils.md5DigestAsHex;
import static org.springframework.util.StringUtils.getFilename;

@Service
@RequiredArgsConstructor
class UserDocumentIngestionUseCase {

    private final DocumentReaderService documentReader;
    private final DocumentIngestionService documentIngestion;


    @SneakyThrows
    void ingest(MultipartFile file) {
        var filename = ofNullable(getFilename(file.getOriginalFilename()))
                .orElseThrow();
        var bytes = file.getBytes();
        var contentHash = md5DigestAsHex(bytes);
        var documents = documentReader.read(new ByteArrayResource(bytes), filename);
        documentIngestion.ingestIfNew(filename, contentHash, documents, USER);
    }
}
