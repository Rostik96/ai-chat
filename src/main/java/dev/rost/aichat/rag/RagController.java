package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.util.StringUtils.getFilename;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
class RagController {

    private final RagService ragService;


    @PostMapping
    @ResponseStatus(NO_CONTENT)
    void upload(@RequestParam("file") MultipartFile file) {
        log.info("RAG user document upload: filename={}", getFilename(file.getOriginalFilename()));
        ragService.ingest(file);
    }
}
