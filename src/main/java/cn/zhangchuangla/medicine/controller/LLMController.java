package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.annotation.Anonymous;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/6 08:45
 */
@RestController
@Anonymous
@RequestMapping("/llm")
public class LLMController {

    private final ChatClient chatClient;


    public LLMController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai")
    String generation(@RequestParam(value = "message", defaultValue = "您好!") String userInput) {
        return this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }
}
