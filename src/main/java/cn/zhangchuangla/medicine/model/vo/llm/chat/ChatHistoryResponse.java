package cn.zhangchuangla.medicine.model.vo.llm.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "会话历史响应")
public class ChatHistoryResponse {

    @Schema(description = "会话UUID")
    private String uuid;

    @Schema(description = "消息列表")
    private List<MessageVO> messages;

    @Data
    @Schema(description = "历史消息")
    public static class MessageVO {
        @Schema(description = "角色")
        private String role;
        @Schema(description = "消息内容")
        private String content;
        @Schema(description = "创建时间")
        private Date createTime;
    }
}

