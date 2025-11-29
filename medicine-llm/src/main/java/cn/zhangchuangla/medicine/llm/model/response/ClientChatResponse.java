package cn.zhangchuangla.medicine.llm.model.response;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.enums.MessageType;
import cn.zhangchuangla.medicine.llm.model.response.card.CardPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
@Data
@Schema(description = "客户端聊天响应")
public class ClientChatResponse {

    @Schema(description = "消息唯一ID（前端去重、追踪用）")
    private String messageId;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "消息发送方角色：USER / ASSISTANT / SYSTEM")
    private MessageRole role;

    @Schema(description = "消息类型：TEXT=文本，CARD=卡片，EVENT=事件，ERROR=错误")
    private MessageType type;

    @Schema(description = "完整文本内容（非必填，流式时可为空，仅返回 delta）")
    private String content;

    @Schema(description = "卡片类型，type=CARD 时必填，如 PRODUCT_LIST、ORDER_STATUS 等")
    private CardType cardType;

    @Schema(description = "卡片数据载荷，type=CARD 时必填")
    private CardPayload payload;

    @Schema(description = "当前回答是否已结束：true 表示本轮不会再有后续消息")
    private Boolean finished;
}
