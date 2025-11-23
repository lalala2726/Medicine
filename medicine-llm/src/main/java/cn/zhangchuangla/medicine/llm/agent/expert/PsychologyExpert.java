package cn.zhangchuangla.medicine.llm.agent.expert;

import cn.zhangchuangla.medicine.llm.agent.ConsultationState;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 心理与精神科专家节点
 *
 * @author Chuang
 * <p>
 * created on 2025/11/20
 */
public class PsychologyExpert implements AsyncNodeAction<ConsultationState> {
    @Override
    public CompletableFuture<Map<String, Object>> apply(ConsultationState state) {
        return null;
    }
}
