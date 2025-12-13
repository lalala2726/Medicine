package cn.zhangchuangla.medicine.llm.workflow;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.workflow.department.dermatology.DermatologyInquiryNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.department.general.GeneralExpertInquiryNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.department.internalmedicine.InternalMedicineInquiryNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.department.surgery.SurgeryInquiryNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.edge.DepartmentRoutingEdgeAction;
import cn.zhangchuangla.medicine.llm.workflow.edge.DiagnosisInfoCheckEdgeAction;
import cn.zhangchuangla.medicine.llm.workflow.node.diagnosis.ExpertDiagnosisNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.node.diagnosis.PreDiagnosisNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.node.review.DiagnosisReviewNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.node.service.GeneralServiceNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.node.start.InitialInquiryNodeAction;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;


/**
 * 专家诊断工作流（StateGraph 配置）
 * <p>
 * 负责：组装“初始询问 -> 业务咨询/基础问诊 -> 科室路由 -> 科室追问 -> 信息检查 -> 诊断 -> 审核 -> 结束”的完整图结构，
 * 并注册各节点（NodeAction）与边（EdgeAction）的跳转关系。
 *
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class ExpertDiagnosisWorkflow {

    private final InitialInquiryNodeAction initialInquiryNodeAction;
    private final GeneralServiceNodeAction generalServiceNodeAction;
    private final PreDiagnosisNodeAction preDiagnosisNodeAction;

    private final InternalMedicineInquiryNodeAction internalMedicineInquiryNodeAction;
    private final SurgeryInquiryNodeAction surgeryInquiryNodeAction;
    private final DermatologyInquiryNodeAction dermatologyInquiryNodeAction;
    private final GeneralExpertInquiryNodeAction generalExpertInquiryNodeAction;

    private final ExpertDiagnosisNodeAction expertDiagnosisNodeAction;
    private final DiagnosisReviewNodeAction diagnosisReviewNodeAction;


    @Bean("llmExpertDiagnosisWorkflow")
    public StateGraph workflow() throws GraphStateException {
        log.info("初始化专家诊断工作流...");
        // 配置状态键策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put(MedicineStateKeyEnum.USER_MESSAGE.getKey(), new ReplaceStrategy());
            keyStrategyHashMap.put(MedicineStateKeyEnum.USER_INTENT.getKey(), new ReplaceStrategy());
            keyStrategyHashMap.put(MedicineStateKeyEnum.SYSTEM_RESPONSE.getKey(), new ReplaceStrategy());
            keyStrategyHashMap.put(WorkflowStateKeys.ROUTE, new ReplaceStrategy());
            keyStrategyHashMap.put(WorkflowStateKeys.SUMMARY, new ReplaceStrategy());
            keyStrategyHashMap.put(WorkflowStateKeys.DIAGNOSIS_RESULT, new ReplaceStrategy());
            keyStrategyHashMap.put(WorkflowStateKeys.FINAL_RESULT, new ReplaceStrategy());
            keyStrategyHashMap.put(WorkflowStateKeys.INQUIRY_ROUND, new ReplaceStrategy());
            return keyStrategyHashMap;
        };

        StateGraph graph = new StateGraph(keyStrategyFactory)
                // 节点注册
                .addNode(WorkflowStateKeys.NODE_INITIAL_INQUIRY, AsyncNodeAction.node_async(initialInquiryNodeAction))
                .addNode(WorkflowStateKeys.NODE_GENERAL_SERVICE, AsyncNodeAction.node_async(generalServiceNodeAction))
                .addNode(WorkflowStateKeys.NODE_PRE_DIAGNOSIS, AsyncNodeAction.node_async(preDiagnosisNodeAction))

                .addNode(WorkflowStateKeys.NODE_INTERNAL_MEDICINE_INQUIRY,
                        AsyncNodeAction.node_async(internalMedicineInquiryNodeAction))
                .addNode(WorkflowStateKeys.NODE_SURGERY_INQUIRY, AsyncNodeAction.node_async(surgeryInquiryNodeAction))
                .addNode(WorkflowStateKeys.NODE_DERMATOLOGY_INQUIRY,
                        AsyncNodeAction.node_async(dermatologyInquiryNodeAction))
                .addNode(WorkflowStateKeys.NODE_GENERAL_EXPERT_INQUIRY,
                        AsyncNodeAction.node_async(generalExpertInquiryNodeAction))

                .addNode(WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS, AsyncNodeAction.node_async(expertDiagnosisNodeAction))
                .addNode(WorkflowStateKeys.NODE_DIAGNOSIS_REVIEW,
                        AsyncNodeAction.node_async(diagnosisReviewNodeAction))

                // 开始 -> 初始询问
                .addEdge(StateGraph.START, WorkflowStateKeys.NODE_INITIAL_INQUIRY)

                // 初始询问 -> 业务咨询 / 进入诊断
                .addConditionalEdges(
                        WorkflowStateKeys.NODE_INITIAL_INQUIRY,
                        edge_async(new InitialInquiryDispatcher()),
                        java.util.Map.of(
                                WorkflowStateKeys.USER_INTENT_GENERAL_SERVICE, WorkflowStateKeys.NODE_GENERAL_SERVICE,
                                WorkflowStateKeys.USER_INTENT_DIAGNOSIS, WorkflowStateKeys.NODE_PRE_DIAGNOSIS
                        )
                )

                // 业务咨询 -> 结束
                .addEdge(WorkflowStateKeys.NODE_GENERAL_SERVICE, StateGraph.END)

                // 基础问诊 -> 根据科室路由分发
                .addConditionalEdges(
                        WorkflowStateKeys.NODE_PRE_DIAGNOSIS,
                        edge_async(new DepartmentRoutingEdgeAction()),
                        java.util.Map.of(
                                WorkflowStateKeys.ROUTE_INTERNAL_MEDICINE,
                                WorkflowStateKeys.NODE_INTERNAL_MEDICINE_INQUIRY,
                                WorkflowStateKeys.ROUTE_SURGERY,
                                WorkflowStateKeys.NODE_SURGERY_INQUIRY,
                                WorkflowStateKeys.ROUTE_DERMATOLOGY,
                                WorkflowStateKeys.NODE_DERMATOLOGY_INQUIRY,
                                WorkflowStateKeys.ROUTE_GENERAL,
                                WorkflowStateKeys.NODE_GENERAL_EXPERT_INQUIRY
                        )
                )

                // 各科室追问 -> 信息检查 -> 追问循环 / 进入诊断
                .addConditionalEdges(
                        WorkflowStateKeys.NODE_INTERNAL_MEDICINE_INQUIRY,
                        edge_async(new DiagnosisInfoCheckEdgeAction(WorkflowStateKeys.NODE_INTERNAL_MEDICINE_INQUIRY)),
                        java.util.Map.of(
                                WorkflowStateKeys.INFO_CHECK_INSUFFICIENT,
                                WorkflowStateKeys.NODE_INTERNAL_MEDICINE_INQUIRY,
                                WorkflowStateKeys.INFO_CHECK_SUFFICIENT,
                                WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS
                        )
                )
                .addConditionalEdges(
                        WorkflowStateKeys.NODE_SURGERY_INQUIRY,
                        edge_async(new DiagnosisInfoCheckEdgeAction(WorkflowStateKeys.NODE_SURGERY_INQUIRY)),
                        java.util.Map.of(
                                WorkflowStateKeys.INFO_CHECK_INSUFFICIENT,
                                WorkflowStateKeys.NODE_SURGERY_INQUIRY,
                                WorkflowStateKeys.INFO_CHECK_SUFFICIENT,
                                WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS
                        )
                )
                .addConditionalEdges(
                        WorkflowStateKeys.NODE_DERMATOLOGY_INQUIRY,
                        edge_async(new DiagnosisInfoCheckEdgeAction(WorkflowStateKeys.NODE_DERMATOLOGY_INQUIRY)),
                        java.util.Map.of(
                                WorkflowStateKeys.INFO_CHECK_INSUFFICIENT,
                                WorkflowStateKeys.NODE_DERMATOLOGY_INQUIRY,
                                WorkflowStateKeys.INFO_CHECK_SUFFICIENT,
                                WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS
                        )
                )
                .addConditionalEdges(
                        WorkflowStateKeys.NODE_GENERAL_EXPERT_INQUIRY,
                        edge_async(new DiagnosisInfoCheckEdgeAction(WorkflowStateKeys.NODE_GENERAL_EXPERT_INQUIRY)),
                        java.util.Map.of(
                                WorkflowStateKeys.INFO_CHECK_INSUFFICIENT,
                                WorkflowStateKeys.NODE_GENERAL_EXPERT_INQUIRY,
                                WorkflowStateKeys.INFO_CHECK_SUFFICIENT,
                                WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS
                        )
                )

                // 诊断 -> 风险审核 -> 结束
                .addEdge(WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS, WorkflowStateKeys.NODE_DIAGNOSIS_REVIEW)
                .addEdge(WorkflowStateKeys.NODE_DIAGNOSIS_REVIEW, StateGraph.END);

        // 输出 PlantUML 便于可视化调试
        GraphRepresentation representation = graph.getGraph(
                GraphRepresentation.Type.PLANTUML,
                "ExpertDiagnosisWorkflow");
        log.info("\n=== 专家诊断工作流 UML ===\n{}\n=========================\n", representation.content());
        return graph;

    }

    /**
     * 工作流检查点保存器（用于中断后恢复）。
     */
    @Bean("llmExpertDiagnosisWorkflowSaver")
    public MemorySaver workflowSaver() {
        return new MemorySaver();
    }

    /**
     * 工作流编译配置：
     * <p>
     * 1) 注册 {@link MemorySaver} 用于 checkpoint 持久化；2) 在诊断节点前设置中断点，支持“先追问再诊断”的两段式交互。
     */
    @Bean("llmExpertDiagnosisWorkflowCompileConfig")
    public CompileConfig workflowCompileConfig(MemorySaver workflowSaver) {
        return CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(workflowSaver).build())
                .interruptBefore(WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS)
                .build();
    }

    static class InitialInquiryDispatcher implements EdgeAction {
        @Override
        public String apply(OverAllState state) {
            String userIntent = state.value(MedicineStateKeyEnum.USER_INTENT.getKey(), String.class).orElse("");
            if (WorkflowStateKeys.USER_INTENT_GENERAL_SERVICE.equalsIgnoreCase(userIntent)) {
                return WorkflowStateKeys.USER_INTENT_GENERAL_SERVICE;
            }
            return WorkflowStateKeys.USER_INTENT_DIAGNOSIS;
        }
    }
}
