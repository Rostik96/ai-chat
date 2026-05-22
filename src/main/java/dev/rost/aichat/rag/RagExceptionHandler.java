package dev.rost.aichat.rag;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
class RagExceptionHandler {

    @ExceptionHandler(UnsupportedDocumentTypeException.class)
    ProblemDetail unsupportedDocumentType(UnsupportedDocumentTypeException e) {
        return ProblemDetail.forStatusAndDetail(BAD_REQUEST, e.getMessage());
    }
}
