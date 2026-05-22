package dev.rost.aichat.rag;

class UnsupportedDocumentTypeException extends RuntimeException {

    UnsupportedDocumentTypeException(String filename) {
        super("Unsupported document type: " + filename);
    }
}
