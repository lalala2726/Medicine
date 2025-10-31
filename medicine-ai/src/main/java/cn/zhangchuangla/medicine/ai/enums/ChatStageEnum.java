package cn.zhangchuangla.medicine.ai.enums;

import com.alibaba.cloud.ai.graph.StateGraph;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 流式聊天阶段枚举
 */
@Getter
public enum ChatStageEnum {

    RECEIVED("RECEIVED", "消息已接收"),
    WORKFLOW_START("WORKFLOW_START", "工作流启动"),
    HEARTBEAT("HEARTBEAT", "心跳保持连接"),
    INTENT_ANALYSIS("INTENT_ANALYSIS", "意图识别"),
    ROUTE_MEDICINE("ROUTE_MEDICINE", "药品咨询处理"),
    ROUTE_CONSULT("ROUTE_CONSULT", "健康咨询处理"),
    ROUTE_OTHER("ROUTE_OTHER", "其他问题处理"),
    TOOL_INVOKE("TOOL_INVOKE", "正在调用工具"),
    TOOL_RESULT("TOOL_RESULT", "工具返回结果"),
    RESPONSE_STREAM("RESPONSE_STREAM", "响应内容流式返回"),
    COMPLETED("COMPLETED", "流程完成"),
    FAILED("FAILED", "流程失败");

    private final String code;
    private final String description;

    ChatStageEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Optional<ChatStageEnum> fromNodeId(String nodeId) {
        if (!StringUtils.hasText(nodeId)) {
            return Optional.empty();
        }
        if (StateGraph.START.equals(nodeId)) {
            return Optional.of(WORKFLOW_START);
        }
        if (StateGraph.END.equals(nodeId)) {
            return Optional.of(COMPLETED);
        }
        if (MedicineNodeEnum.INTENT.getNodeId().equals(nodeId)) {
            return Optional.of(INTENT_ANALYSIS);
        }
        if (MedicineNodeEnum.MEDICINE.getNodeId().equals(nodeId)) {
            return Optional.of(ROUTE_MEDICINE);
        }
        if (MedicineNodeEnum.CONSULT.getNodeId().equals(nodeId)) {
            return Optional.of(ROUTE_CONSULT);
        }
        if (MedicineNodeEnum.OTHER.getNodeId().equals(nodeId)) {
            return Optional.of(ROUTE_OTHER);
        }
        return Optional.empty();
    }

    public boolean isResponseStage() {
        return this == ROUTE_MEDICINE || this == ROUTE_CONSULT || this == ROUTE_OTHER;
    }
}
