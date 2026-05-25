package dev.rost.aichat.rag;

import lombok.SneakyThrows;
import org.jspecify.annotations.NullMarked;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@lombok.Builder(builderClassName = "Builder")
public final class RoutingQueryAugmenter implements QueryAugmenter {

    private final PromptTemplate noRetrievalPromptTemplate;
    private final ContextualQueryAugmenter withContextAugmenter;


    private RoutingQueryAugmenter(
            PromptTemplate noRetrievalPromptTemplate,
            ContextualQueryAugmenter withContextAugmenter) {
        this.noRetrievalPromptTemplate = noRetrievalPromptTemplate;
        this.withContextAugmenter = withContextAugmenter;
    }

    @Override
    @NullMarked
    public Query augment(Query query, List<Document> documents) {
        if (documents.isEmpty())
            return new Query(noRetrievalPromptTemplate.render(Map.of("query", query.text())));
        return withContextAugmenter.augment(query, documents);
    }


    public static class Builder {
        private Resource noRetrievalPromptTemplateResource;
        private Resource withContextPromptTemplateResource;

        public Builder noRetrievalPromptTemplateResource(Resource noRetrievalPromptTemplateResource) {
            this.noRetrievalPromptTemplateResource = noRetrievalPromptTemplateResource;
            return this;
        }

        public Builder withContextPromptTemplateResource(Resource withContextPromptTemplateResource) {
            this.withContextPromptTemplateResource = withContextPromptTemplateResource;
            return this;
        }

        @SneakyThrows
        public RoutingQueryAugmenter build() {
            return new RoutingQueryAugmenter(
                    new PromptTemplate(noRetrievalPromptTemplateResource.getContentAsString(UTF_8)),
                    ContextualQueryAugmenter.builder()
                            .allowEmptyContext(true)
                            .promptTemplate(new PromptTemplate(withContextPromptTemplateResource))
                            .build());
        }
    }
}
