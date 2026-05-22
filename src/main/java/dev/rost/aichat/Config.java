package dev.rost.aichat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
class Config {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, List<Advisor> advisors) {
        return builder
                .defaultAdvisors(advisors)
                .build();
    }

    @Bean
    Advisor chatMemoryAdvisor(ChatMemoryRepository chatMemoryRepository, @Value("${chat.memory.max-messages:20}") int maxMessages) {
        return MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build()).build();
    }


    @Bean
    Advisor ragAdvisor(
            VectorStore vectorStore,
            @Value("default-prompt-template.txt") ClassPathResource promptTemplate) throws IOException {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(new PromptTemplate(promptTemplate.getContentAsString(StandardCharsets.UTF_8)))
                .build();
    }
}
