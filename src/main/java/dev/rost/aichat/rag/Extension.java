package dev.rost.aichat.rag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.getFilenameExtension;

@Getter
@RequiredArgsConstructor
enum Extension {
    TXT("txt"),
    PDF("pdf"),
    DOC("doc"),
    DOCX("docx");

    private final String name;
    private static final Map<String, Extension> extByValue = Stream.of(values())
            .collect(toMap(ext -> ext.name, identity()));

    static Extension of(String filename) {
        return ofNullable(getFilenameExtension(filename))
                .map(v -> extByValue.get(v.toLowerCase(Locale.ROOT)))
                .orElseThrow(() -> new UnsupportedDocumentTypeException(filename));
    }
}
