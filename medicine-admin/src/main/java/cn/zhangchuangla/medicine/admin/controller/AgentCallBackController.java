package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeImportCallbackRequest;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2026/3/3
 */
@RestController
@RequestMapping("/agent/callback")
@Tag(name = "智能体回调接口，请不要主动调用", description = "智能体回调接口，请不要主动调用")
public class AgentCallBackController {


    @PostMapping("/knowlage_base_import")
    @Operation(
            summary = "知识库导入回调",
            description = "导入知识库之后 Agent 会在导入成功后执行此回调,开发人员不要主动调用此接口"
    )
    @Anonymous
    public String knowlageBaseImport(@RequestBody KnowledgeImportCallbackRequest request) {
        System.out.println(request);
        //todo 这边需要对参数进行校验，防止恶意调用
        return "SUCCESS";
    }
}
