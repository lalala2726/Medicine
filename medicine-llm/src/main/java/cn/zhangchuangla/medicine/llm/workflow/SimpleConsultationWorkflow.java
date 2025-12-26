package cn.zhangchuangla.medicine.llm.workflow;

import cn.zhangchuangla.medicine.llm.service.node.DiagnosisCardGenerationNode;
import cn.zhangchuangla.medicine.llm.service.node.EmotionalSupportNode;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 简单咨询工作流：快速安抚与完整咨询并行执行。
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SimpleConsultationWorkflow {

    private static final String NODE_FAST_REPLY = DiagnosisCardGenerationNode.NODE_ID;
    private static final String NODE_FULL_REPLY = EmotionalSupportNode.NODE_ID;

    private final DiagnosisCardGenerationNode diagnosisCardGenerationNode;
    private final EmotionalSupportNode emotionalSupportNode;

    @Bean("llmSimpleConsultationWorkflow")
    public StateGraph workflow() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("userMessage", new ReplaceStrategy());
            keyStrategyHashMap.put("quickReply", new ReplaceStrategy());
            keyStrategyHashMap.put("finalReply", new ReplaceStrategy());
            return keyStrategyHashMap;
        };

        StateGraph graph = new StateGraph(keyStrategyFactory)
                .addNode(NODE_FAST_REPLY, AsyncNodeAction.node_async(diagnosisCardGenerationNode))
                .addNode(NODE_FULL_REPLY, AsyncNodeAction.node_async(emotionalSupportNode))
                .addEdge(StateGraph.START, NODE_FAST_REPLY)
                .addEdge(StateGraph.START, NODE_FULL_REPLY)
                .addEdge(NODE_FAST_REPLY, StateGraph.END)
                .addEdge(NODE_FULL_REPLY, StateGraph.END);

        GraphRepresentation representation = graph.getGraph(
                GraphRepresentation.Type.PLANTUML,
                "SimpleConsultationWorkflow");
        log.info("\n=== 简单咨询工作流 UML ===\n{}\n=========================\n", representation.content());
        return graph;
    }
}
