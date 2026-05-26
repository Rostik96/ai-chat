package dev.rost.aichat.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@NullMarked
@RequiredArgsConstructor
public class PersonalQueryTransformer implements QueryTransformer {

    private final ChatModel model;
    private static final String template = """
            If the question contains any second-person references in Russian (such as "ты", "тебе", "тебя", "тобой", etc.), reformulate the question by addressing Ростислав Жистовский directly as "ты".
            If there are no such references, return the question unchanged.
            Question: {question}""";


    @Override
    public Query transform(Query query) {
        log.info("PersonalQueryTransformer#transform");
        var prompt = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("question", query.text()))
                .build()
                .create();
        var text = Optional.of(model.call(prompt))
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElseThrow(() -> new IllegalStateException("Chat model returned empty response"));

        return query.mutate()
                .text(text)
                .build();
    }
}
