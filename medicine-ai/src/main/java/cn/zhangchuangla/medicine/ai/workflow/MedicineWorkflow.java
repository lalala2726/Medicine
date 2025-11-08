package cn.zhangchuangla.medicine.ai.workflow;

import cn.zhangchuangla.medicine.ai.enums.MedicineNodeEnum;
import cn.zhangchuangla.medicine.ai.enums.UserIntentEnum;
import cn.zhangchuangla.medicine.ai.workflow.node.ConsultNode;
import cn.zhangchuangla.medicine.ai.workflow.node.IntentNode;
import cn.zhangchuangla.medicine.ai.workflow.node.MedicineNode;
import cn.zhangchuangla.medicine.ai.workflow.node.OtherNode;
import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import com.alibaba.cloud.ai.graph.*;
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
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 医疗工作流配置类
 * 基于Spring AI Alibaba Graph实现医疗咨询工作流
 *
 * @author Chuang
 * created 2025/9/10
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class MedicineWorkflow {

    private final ConsultNode consultNode;
    private final MedicineNode medicineNode;
    private final OtherNode otherNode;
    private final IntentNode intentNode;

    @Bean(name = "medicineWorkflowService")
    public StateGraph medicineWorkflowService() throws GraphStateException {
        log.info("初始化医疗工作流...");

        // 配置状态键策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> m = new HashMap<>();
            m.put(MedicineStateKeyEnum.USER_MESSAGE.getKey(), new ReplaceStrategy());
            m.put(MedicineStateKeyEnum.USER_INTENT.getKey(), new ReplaceStrategy());
            m.put(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey(), new ReplaceStrategy());
            return m;
        };

        // 构建 StateGraph
        StateGraph graph = new StateGraph(keyStrategyFactory)
                // 添加节点
                .addNode(MedicineNodeEnum.INTENT.getNodeId(), node_async(intentNode))
                .addNode(MedicineNodeEnum.MEDICINE.getNodeId(), node_async(medicineNode))
                .addNode(MedicineNodeEnum.CONSULT.getNodeId(), node_async(consultNode))
                .addNode(MedicineNodeEnum.OTHER.getNodeId(), node_async(otherNode))

                // 起点 → 意图识别
                .addEdge(StateGraph.START, MedicineNodeEnum.INTENT.getNodeId())

                // 条件分发：IntentDispatcher 返回对应的节点ID
                .addConditionalEdges(MedicineNodeEnum.INTENT.getNodeId(), edge_async(new IntentDispatcher()),
                        Map.of(
                                UserIntentEnum.MEDICINE.getIntent(), MedicineNodeEnum.MEDICINE.getNodeId(),
                                UserIntentEnum.CONSULT.getIntent(), MedicineNodeEnum.CONSULT.getNodeId(),
                                UserIntentEnum.OTHER.getIntent(), MedicineNodeEnum.OTHER.getNodeId()
                        ))

                // 三个终端节点都走到 END
                .addEdge(MedicineNodeEnum.MEDICINE.getNodeId(), StateGraph.END)
                .addEdge(MedicineNodeEnum.CONSULT.getNodeId(), StateGraph.END)
                .addEdge(MedicineNodeEnum.OTHER.getNodeId(), StateGraph.END);

        // 输出 PlantUML 便于可视化调试
        GraphRepresentation representation = graph.getGraph(
                GraphRepresentation.Type.PLANTUML,
                "MedicalAssistant");
        log.info("\n=== 医疗助手工作流 UML ===\n{}\n=========================\n", representation.content());

        return graph;
    }

    /**
     * 意图分发器：根据用户意图决定下一跳节点
     * 使用枚举确保类型安全
     */
    static class IntentDispatcher implements EdgeAction {
        @Override
        public String apply(OverAllState state) {
            String intent = String.valueOf(state.value(MedicineStateKeyEnum.USER_INTENT.getKey()));
            UserIntentEnum userIntentEnum = UserIntentEnum.fromString(intent);

            log.debug("意图分发: {} -> {}", intent, userIntentEnum.getNodeId());

            // 返回对应的节点ID
            return userIntentEnum.getNodeId();
        }
    }
}
