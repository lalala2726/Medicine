package cn.zhangchuangla.medicine.enums;

import com.alibaba.cloud.ai.graph.StateGraph;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 流式聊天阶段枚举
 */
public enum ChatStageEnum {

    RECEIVED("received", "消息已接收"),
    WORKFLOW_START("workflow_start", "工作流启动"),
    INTENT_ANALYSIS("intent_analysis", "意图识别"),
    ROUTE_MEDICINE("route_medicine", "药品咨询处理"),
    ROUTE_CONSULT("route_consult", "健康咨询处理"),
    ROUTE_OTHER("route_other", "其他问题处理"),
    RESPONSE_STREAM("response_stream", "响应内容流式返回"),
    COMPLETED("completed", "流程完成"),
    FAILED("failed", "流程失败");

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

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isResponseStage() {
        return this == ROUTE_MEDICINE || this == ROUTE_CONSULT || this == ROUTE_OTHER;
    }
}
