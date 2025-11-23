package cn.zhangchuangla.medicine.llm.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/22
 */
@Service
public class ConsultationService {

    private final ChatClient chatClient;

    public ConsultationService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    public Flux<String> simpleConsultation(String question) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("你必须重复用户的每一句话");
        Prompt prompt = systemPromptTemplate.create();
        return chatClient
                .prompt(prompt)
                .user(question)
                .stream()
                .content();
    }
    


}
