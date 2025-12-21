package cn.zhangchuangla.medicine.llm.workflow;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.workflow.node.RouteNode;
import cn.zhangchuangla.medicine.llm.workflow.node.expert.DermatologyExpertNode;
import cn.zhangchuangla.medicine.llm.workflow.node.expert.InternalMedicineExpertNode;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;


/**
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class ExpertDiagnosisWorkflow {

    private final DermatologyExpertNode dermatologyExpertNode;
    private final InternalMedicineExpertNode internalMedicineExpertNode;
    private final RouteNode routeNode;


    @Bean("llmExpertDiagnosisWorkflow")
    public StateGraph workflow() throws GraphStateException {
        log.info("初始化专家诊断工作流...");
        // 配置状态键策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put(MedicineStateKeyEnum.USER_MESSAGE.getKey(), new ReplaceStrategy());
            keyStrategyHashMap.put(MedicineStateKeyEnum.USER_INTENT.getKey(), new ReplaceStrategy());
            keyStrategyHashMap.put(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey(), new ReplaceStrategy());
            keyStrategyHashMap.put("route", new ReplaceStrategy());
            keyStrategyHashMap.put("diagnosisResult", new ReplaceStrategy());
            return keyStrategyHashMap;
        };

        StateGraph graph = new StateGraph(keyStrategyFactory)
                // 节点注册
                .addNode("RouteNode", AsyncNodeAction.node_async(routeNode))
                .addNode("DermatologyExpertNode", AsyncNodeAction.node_async(dermatologyExpertNode))
                .addNode("InternalMedicineExpertNode", AsyncNodeAction.node_async(internalMedicineExpertNode))
                // 开始 -> 路由
                .addEdge(StateGraph.START, "RouteNode")
                // 根据意图分发到具体专家
                .addConditionalEdges("RouteNode", edge_async(new IntentDispatcher()),
                        Map.of(
                                "DermatologyExpertNode", "DermatologyExpertNode",
                                "InternalMedicineExpertNode", "InternalMedicineExpertNode"
                        )
                )
                // 暂时取消 ReviewNode，专家节点直接结束
                .addEdge("DermatologyExpertNode", StateGraph.END)
                .addEdge("InternalMedicineExpertNode", StateGraph.END);
        // 输出 PlantUML 便于可视化调试
        GraphRepresentation representation = graph.getGraph(
                GraphRepresentation.Type.PLANTUML,
                "ExpertDiagnosisWorkflow");
        log.info("\n=== 专家诊断工作流 UML ===\n{}\n=========================\n", representation.content());
        return graph;

    }

    static class IntentDispatcher implements EdgeAction {
        @Override
        public String apply(OverAllState state) {
            String route = state.value("route", "").trim();
            return switch (route) {
                case "DermatologyExpertNode" -> "DermatologyExpertNode";
                case "InternalMedicineExpertNode" -> "InternalMedicineExpertNode";
                default -> "InternalMedicineExpertNode";
            };
        }
    }

}
