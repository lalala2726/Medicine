package cn.zhangchuangla.medicine.llm.workflow.node.diagnosis;

import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.llm.exection.LLMParamException;
import cn.zhangchuangla.medicine.llm.workflow.support.DepartmentDiagnosisAction;
import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 专家诊断节点（Expert Diagnosis）
 * <p>
 * 负责：根据路由结果选择对应科室的诊断实现，基于当前摘要输出诊断建议。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpertDiagnosisNodeAction implements NodeAction {

    private final Map<String, DepartmentDiagnosisAction> diagnosisActions;

    @Override
    public Map<String, Object> apply(OverAllState state){
        log.info("【工作流】进入节点：{}", WorkflowStateKeys.NODE_EXPERT_DIAGNOSIS);
        String route = state.value(WorkflowStateKeys.ROUTE, String.class).orElse(WorkflowStateKeys.ROUTE_GENERAL);
        log.info("【工作流】诊断科室路由：{}", route);
        String symptomInfo = state.value(MedicineStateKeyEnum.USER_MESSAGE.getKey(), String.class).orElse("");
        String inquiryAnswer = state.value(WorkflowStateKeys.INQUIRY_ANSWER, String.class).orElse(null);
        if (inquiryAnswer != null && !inquiryAnswer.isBlank()) {
            symptomInfo = symptomInfo + "\n\n【用户补充回答】\n" + inquiryAnswer;
        }

        DepartmentDiagnosisAction diagnosisAction = diagnosisActions.get(route);
        if (diagnosisAction == null) {
            diagnosisAction = diagnosisActions.get(WorkflowStateKeys.ROUTE_GENERAL);
        }
        if (diagnosisAction == null) {
            throw new LLMParamException("未找到可用的科室诊断实现: " + route);
        }

        String diagnosisResult = diagnosisAction.diagnose(symptomInfo);
        if (diagnosisResult == null) {
            throw new LLMParamException("专家诊断结果为空");
        }
        return Map.of(WorkflowStateKeys.DIAGNOSIS_RESULT, diagnosisResult);
    }
}
