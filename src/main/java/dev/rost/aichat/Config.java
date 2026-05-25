package dev.rost.aichat;

import dev.rost.aichat.rag.RoutingQueryAugmenter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

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
    @Order(1)
    Advisor chatMemoryAdvisor(ChatMemoryRepository chatMemoryRepository, @Value("${chat.memory.max-messages:20}") int maxMessages) {
        return MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build()).build();
    }


    @Bean
    @Order(2)
    Advisor logAdvisor() {
        return SimpleLoggerAdvisor.builder().build();
    }


    @Bean
    @Order(3)
    Advisor ragAdvisor(
            VectorStore vectorStore,
            @Value("${rag.search.similarity-threshold}") double similarityThreshold,
            @Value("${rag.search.top-k}") int topK,
            @Value("rag-query-augmenter-prompt.txt") ClassPathResource withContextPromptTemplate,
            @Value("rag-query-augmenter-no-retrieval-prompt.txt") ClassPathResource noRetrievalPromptTemplate) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(similarityThreshold)
                        .topK(topK)
                        .build())
                .queryAugmenter(RoutingQueryAugmenter.builder()
                        .noRetrievalPromptTemplateResource(noRetrievalPromptTemplate)
                        .withContextPromptTemplateResource(withContextPromptTemplate)
                        .build())
                .build();
    }
}
