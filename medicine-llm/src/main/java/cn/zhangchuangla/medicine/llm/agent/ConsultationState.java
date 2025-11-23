package cn.zhangchuangla.medicine.llm.agent;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.Map;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/22
 */
public class ConsultationState extends AgentState {

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            "patientQuestion", Channels.base(() -> ""),      // 患者问题
            "symptoms", Channels.base(() -> ""),             // 症状描述
            "department", Channels.base(() -> ""),           // 科室分类
            "diagnosis", Channels.base(() -> ""),            // 诊断结果
            "treatmentAdvice", Channels.base(() -> ""),      // 治疗建议
            "conversationHistory", Channels.base(() -> "")   // 对话历史
    );


    /**
     * Constructs an AgentState with the given initial data.
     *
     * @param initData the initial data for the agent state
     */
    public ConsultationState(Map<String, Object> initData) {
        super(initData);
    }
}
