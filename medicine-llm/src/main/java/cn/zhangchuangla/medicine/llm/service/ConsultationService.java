package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.enums.MessageType;
import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import cn.zhangchuangla.medicine.llm.model.response.card.CardPayload;
import cn.zhangchuangla.medicine.llm.model.response.card.MedicineRecommendPayload;
import cn.zhangchuangla.medicine.llm.model.response.card.ProductCardPayload;
import cn.zhangchuangla.medicine.llm.model.response.card.SymptomSelectorPayload;
import cn.zhangchuangla.medicine.llm.tool.ClientConsultationTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/22
 */
@Service
public class ConsultationService {

    private static final String CONSULTATION_SYSTEM_PROMPT = """
            角色：你是药房智能问诊助手，只能输出给用户看的内容，不得暴露系统、工具或示例。
            目标：根据用户诉求决定是否返回卡片消息（symptom-selector / medicine-recommend / product-card）或纯文本。
            规则：
            1) 需要展示卡片时，先了解卡片字段定义，按需调用已注册的工具获取真实数据（禁止使用示例数据），生成包含 type 字段的 content。
            2) 真实数据必须来源于对话理解或工具返回，禁止泄露 chatExample.md 的示例值，禁止输出“根据示例/工具返回”的描述。
            3) 纯文本可流式输出；卡片类消息一次性完整返回，包含完整 data，且不追加示例。
            4) 不清楚信息时，用文本向用户澄清，不要编造。
            """;

    private final ChatClient chatClient;
    private final ClientConsultationTools clientConsultationTools;
    private final ObjectMapper objectMapper;

    public ConsultationService(ChatClient chatClient,
                               ClientConsultationTools clientConsultationTools,
                               ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.clientConsultationTools = clientConsultationTools;
        this.objectMapper = objectMapper;
    }

    public Flux<ClientChatResponse> simpleConsultation(String question) {
        Flux<ClientChatResponse> responses = chatClient
                .prompt()
                .system(CONSULTATION_SYSTEM_PROMPT)
                .user(question)
                .tools(clientConsultationTools)
                .stream()
                .content()
                .map(content -> buildResponse(content, false));

        ClientChatResponse finished = buildResponse("", true);
        return responses.concatWithValues(finished)
                .takeUntil(ClientChatResponse::getFinished);
    }

    private ClientChatResponse buildResponse(String content, boolean finished) {
        ClientChatResponse response = new ClientChatResponse();
        response.setMessageId(UUID.randomUUID().toString());
        response.setSessionId(null);
        response.setRole(MessageRole.ASSISTANT);
        response.setFinished(finished);

        if (content == null || content.isBlank()) {
            response.setType(MessageType.EVENT);
            response.setContent("");
            return response;
        }

        if (tryFillCard(content, response)) {
            return response;
        }

        response.setType(MessageType.TEXT);
        response.setContent(content);
        return response;
    }

    private boolean tryFillCard(String content, ClientChatResponse response) {
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode typeNode = root.get("type");
            JsonNode dataNode = root.get("data");
            if (typeNode == null || dataNode == null) {
                return false;
            }
            CardType cardType = CardType.fromValue(typeNode.asText());
            if (cardType == null) {
                return false;
            }
            response.setType(MessageType.CARD);
            response.setCardType(cardType);
            response.setPayload(mapPayload(cardType, dataNode));
            response.setFinished(true);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private CardPayload mapPayload(CardType cardType, JsonNode dataNode) throws Exception {
        return switch (cardType) {
            case SYMPTOM_SELECTOR -> objectMapper.treeToValue(dataNode, SymptomSelectorPayload.class);
            case MEDICINE_RECOMMEND -> objectMapper.treeToValue(dataNode, MedicineRecommendPayload.class);
            case PRODUCT_CARD -> objectMapper.treeToValue(dataNode, ProductCardPayload.class);
        };
    }
}
