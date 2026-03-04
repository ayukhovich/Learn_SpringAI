package org.learn.springai;

import org.learn.springai.advisors.expension.ExpansionQueryAdvisor;
import org.learn.springai.advisors.rag.RagAdvisor;
import org.learn.springai.repo.ChatRepository;
import org.learn.springai.services.PostgresChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoWebinar1NoReactApplication {
    //@Value("${max_leghth_for_aswer}")

    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate(
            "{query}\n\n" +
                    "Контекст:\n" +
                    "---------------------\n" +
                    "{question_answer_context}\n" +
                    "---------------------\n\n" +
                    "Отвечай только на основе контекста выше. Если информации нет в контексте, сообщи, что не можешь ответить."
    );


    private static final PromptTemplate SYSTEM_PROMPT = new PromptTemplate(
            """
            Ты - Евгений Борисов, Java-разработчик и эксперт по Spring. Отвечай от первого лица, кратко и по делу.
            
            Вопрос может быть о СЛЕДСТВИИ факта из Context.
            ВСЕГДА связывай: факт Context -> вопрос.
    
            Нет связи, даже косвенной = "я не говорил об этом в докладах".
            Есть связь = отвечай.
            """
            );

    @Autowired
    private ChatRepository  chatRepository;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatModel chatModel;;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        ExpansionQueryAdvisor.builder(chatModel).order(0).build(),
                        getHistoryAdvisor(1),
                    SimpleLoggerAdvisor.builder().order(2).build(),
                    RagAdvisor.build(vectorStore).order(3).build(),
                    //getRagAdviser(3),
                    SimpleLoggerAdvisor.builder().order(4).build()
                )
                .defaultOptions(getOllamaOptions())
                .defaultSystem(SYSTEM_PROMPT.render())
                .build();
    }

    private Advisor getHistoryAdvisor(int order) {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).order(order).build();
    }

    private Advisor getRagAdviser(int order) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .searchRequest(
                    SearchRequest.builder()
                            .topK(4)
                            .similarityThreshold(0.60)
                            .build()
                )
                .order(order)
                .build();
    }

    private ChatMemory getChatMemory() {
        return PostgresChatMemory.builder()
                .maxMessages(8)
                .chatMemoryRepository(chatRepository)
                .build();
    }

    private OllamaOptions getOllamaOptions() {
        return OllamaOptions.builder()
                // temperature: 0.0 - deterministic, 1.0 - creative (default 0.8)
                .temperature(0.3)
                // topP: nucleus sampling - cumulative probability of tokens (default 0.9)
                .topP(0.7)
                // topK: number of most probable tokens to consider (default 40)
                .topK(20)
                // repeatPenalty: penalizes repeated tokens (1.0 = no penalty, >1.0 = penalty)
                .repeatPenalty(1.1)
                .build();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoWebinar1NoReactApplication.class, args);
        ChatClient chatClient = context.getBean(ChatClient.class);
    }
}
