package cn.zhangchuangla.medicine.llm.workflow.edge;

import cn.zhangchuangla.medicine.llm.workflow.support.WorkflowStateKeys;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.Getter;

/**
 * 信息完整性判断边（Diagnosis Info Check Edge）
 * <p>
 * 负责：根据追问轮次（inquiryRound）判断信息是否充分，返回“继续追问”或“进入诊断”的分支标签。
 * <p>
 * 说明：在“人工介入（Human-in-the-Loop）”模式下，首次运行会在诊断节点前中断，
 * 因此这里默认直接进入诊断分支；后续可替换为 LLM/规则的完整性校验逻辑。
 */
@Getter
public class DiagnosisInfoCheckEdgeAction implements EdgeAction {

    private final String nodeName;

    public DiagnosisInfoCheckEdgeAction(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public String apply(OverAllState state) {
        return WorkflowStateKeys.INFO_CHECK_SUFFICIENT;
    }

}
