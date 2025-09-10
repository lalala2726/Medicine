package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.annotation.Anonymous;
import cn.zhangchuangla.medicine.llm.service.OpenAiClientFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/10 15:21
 */
@RequestMapping("/llm/chat")
@RequiredArgsConstructor
@Tag(name = "LLM聊天接口", description = "LLM聊天接口")
@Anonymous
@RestController
public class LLMChatController {

    private final OpenAiClientFactory openAiClientFactory;


    @GetMapping("/test")
    public String chat(@RequestParam(value = "message", defaultValue = "您好!") String message) {
        ChatClient chatClient = openAiClientFactory.chatClient();
        return chatClient.prompt()
                .user(message)
                .call()
                .content();

    }
}
