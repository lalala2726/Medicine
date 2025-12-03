package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.enums.MedicineStateKeyEnum;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/2
 */
@RestController
@RequestMapping("/workflow")
@Tag(name = "工作流测试")
@Anonymous
public class WorkFlowTestController extends BaseController {

    private final StateGraph expertDiagnosisWorkflow;

    public WorkFlowTestController(@Qualifier("llmExpertDiagnosisWorkflow") StateGraph expertDiagnosisWorkflow) {
        this.expertDiagnosisWorkflow = expertDiagnosisWorkflow;
    }

    @PostMapping("/expert/run")
    @Operation(summary = "运行专家诊断工作流", description = "输入用户描述，返回工作流最终状态")
    public AjaxResult<Map<String, Object>> run(@RequestBody WorkFlowTestRequest request) throws GraphStateException {
        CompiledGraph compiledGraph = expertDiagnosisWorkflow.compile();

        Map<String, Object> initialState = new HashMap<>();
        initialState.put(MedicineStateKeyEnum.USER_MESSAGE.getKey(), request.getMessage());
        // 兼容现有节点使用的入参键
        initialState.put("summer", request.getMessage());

        OverAllState resultState = compiledGraph.invoke(initialState)
                .orElseThrow(() -> new IllegalStateException("工作流执行未返回结果"));

        return success(resultState.data());
    }

    @Data
    private static class WorkFlowTestRequest {
        /**
         * 用户输入的病情描述
         */
        private String message;
    }

}
