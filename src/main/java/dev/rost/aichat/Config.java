package dev.rost.aichat;

import dev.rost.aichat.rag.Bm25RerankDocumentPostProcessor;
import dev.rost.aichat.rag.PersonalQueryTransformer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
class Config {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }


    @Bean
    ChatClientCustomizer defaultAdvisorsCustomizer(List<Advisor> advisors) {
        return builder -> builder.defaultAdvisors(advisors);
    }

    @Bean
    Advisor ragAdvisor(
            VectorStore vectorStore,
            PersonalQueryTransformer personalQueryTransformer,
            @Value("classpath:rag-query-augmenter-prompt.txt") Resource ragPromptTemplate,
            @Value("classpath:rag-query-augmenter-no-retrieval-prompt.txt") Resource ragNoRetrievalPromptTemplate,
            @Value("${rag.search.similarity-threshold}") double similarityThreshold,
            @Value("${rag.search.top-k}") int topK) {
        return RetrievalAugmentationAdvisor.builder()
                .order(0)
                .queryTransformers(personalQueryTransformer)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(false)
                        .promptTemplate(new PromptTemplate(ragPromptTemplate))
                        .emptyContextPromptTemplate(new PromptTemplate(ragNoRetrievalPromptTemplate))
                        .build())
                .documentPostProcessors(new Bm25RerankDocumentPostProcessor(topK))
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(similarityThreshold)
                        .topK(topK * 2)
                        .build())
                .build();
    }


    @Bean
    Advisor chatMemoryAdvisor(ChatMemoryRepository chatMemoryRepository, @Value("${chat.memory.max-messages:20}") int maxMessages) {
        return MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                        .chatMemoryRepository(chatMemoryRepository)
                        .maxMessages(maxMessages)
                        .build())
                .order(1)
                .build();
    }


    @Bean
    Advisor logAdvisor() {
        return SimpleLoggerAdvisor.builder()
                .order(2)
                .build();
    }
}
