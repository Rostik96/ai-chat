package dev.rost.aichat.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class DocumentReaderService {

    List<Document> read(Resource resource, String filename) {
        var ext = Extension.of(filename);
        return switch (ext) {
            case TXT -> new TextReader(resource).get();
            case PDF, DOC, DOCX -> new TikaDocumentReader(resource).get();
        };
    }
}
