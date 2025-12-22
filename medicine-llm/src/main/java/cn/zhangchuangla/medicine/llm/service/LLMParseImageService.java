package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import cn.zhangchuangla.medicine.llm.prompt.SystemPrompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.util.List;

/**
 * 大模型解析图片相关服务
 *
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMParseImageService {

    private static final String DASH_SCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode";

    //todo 后续能在线配置
    private final OpenAiApi baseOpenAiApi;
    private final OpenAiChatModel baseChatModel;

    public DrugInfoDto parseImage(List<String> imageBase64List) {
        Assert.notEmpty(imageBase64List, "图片不能为空");
        OpenAiApi qwenClient = baseOpenAiApi.mutate()
                .baseUrl(DASH_SCOPE_BASE_URL)
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .build();

        OpenAiChatModel qwen3vlPlus = baseChatModel.mutate()
                .openAiApi(qwenClient)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen3-vl-plus")
                        .temperature(0.0)
                        .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_OBJECT, null))
                        .build())
                .build();

        List<Media> mediaList = imageBase64List.stream()
                .map(base64Str -> new Media(MimeTypeUtils.IMAGE_PNG, URI.create(base64Str)))
                .toList();

        // 创建包含图片的用户消息
        UserMessage userMessage = UserMessage.builder()
                .media(mediaList)
                .text(SystemPrompt.DRUG_PARSER_PROMPT)
                .build();

        ChatClient customClient = ChatClient.builder(qwen3vlPlus)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();


        return customClient.prompt()
                .messages(userMessage)
                .call()
                .entity(DrugInfoDto.class);
    }


}
