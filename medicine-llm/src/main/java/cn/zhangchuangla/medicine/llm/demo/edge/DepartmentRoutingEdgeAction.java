package cn.zhangchuangla.medicine.llm.demo.edge;

import cn.zhangchuangla.medicine.llm.demo.support.WorkflowStateKeys;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * 科室路由边（Department Routing Edge）
 * <p>
 * 负责：读取 state 中的路由结果（route），并返回标准化的路由分支标签，用于从“基础问诊”节点跳转到具体科室追问节点。
 */
public class DepartmentRoutingEdgeAction implements EdgeAction {

    @Override
    public String apply(OverAllState state) {
        String route = state.value(WorkflowStateKeys.ROUTE, String.class).orElse(WorkflowStateKeys.ROUTE_GENERAL);
        return switch (route) {
            case WorkflowStateKeys.ROUTE_INTERNAL_MEDICINE -> WorkflowStateKeys.ROUTE_INTERNAL_MEDICINE;
            case WorkflowStateKeys.ROUTE_SURGERY -> WorkflowStateKeys.ROUTE_SURGERY;
            case WorkflowStateKeys.ROUTE_DERMATOLOGY -> WorkflowStateKeys.ROUTE_DERMATOLOGY;
            default -> WorkflowStateKeys.ROUTE_GENERAL;
        };
    }
}
