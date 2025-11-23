package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import cn.zhangchuangla.medicine.llm.prompt.SystemPrompt;
import com.alibaba.fastjson.JSON;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 大模型解析图片相关服务
 *
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
@Service
public class LLMParseImageService {

    private final OpenAiApi baseOpenAiApi;

    public LLMParseImageService(OpenAiApi baseOpenAiApi) {
        this.baseOpenAiApi = baseOpenAiApi;
    }


    public DrugInfoDto parseImage(List<String> imageBase64List) {
        Assert.notEmpty(imageBase64List, "图片不能为空");

        OpenAiApi qwenClient = baseOpenAiApi.mutate()
                // todo 这边后续统一进行配置
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode")
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .build();

        // 构造多模态消息：多张图片 + 文本指令
        List<OpenAiApi.ChatCompletionMessage.MediaContent> userContent = new ArrayList<>();
        for (String imageBase64 : imageBase64List) {
            Assert.hasText(imageBase64, "图片不能为空");
            userContent.add(new OpenAiApi.ChatCompletionMessage.MediaContent(
                    new OpenAiApi.ChatCompletionMessage.MediaContent.ImageUrl(imageBase64)));
        }
        userContent.add(new OpenAiApi.ChatCompletionMessage.MediaContent("请根据图片识别药品信息并输出 JSON。"));

        List<OpenAiApi.ChatCompletionMessage> messages = List.of(
                new OpenAiApi.ChatCompletionMessage(SystemPrompt.DRUG_PARSER_PROMPT,
                        OpenAiApi.ChatCompletionMessage.Role.SYSTEM),
                new OpenAiApi.ChatCompletionMessage(userContent, OpenAiApi.ChatCompletionMessage.Role.USER)
        );

        OpenAiApi.ChatCompletionRequest chatRequest = new OpenAiApi.ChatCompletionRequest(messages,
                "qwen3-vl-flash", 0.0);

        String content = Objects.requireNonNull(qwenClient.chatCompletionEntity(chatRequest)
                        .getBody())
                .choices()
                .getFirst()
                .message()
                .content();

        return JSON.parseObject(content, DrugInfoDto.class);
    }

}
