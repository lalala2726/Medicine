package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/4
 */
@RequestMapping("/knowledge_base")
public class KnowledgeBaseController extends BaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping("/list")
    @Operation(summary = "知识库列表")
    public AjaxResult<Void> knowledgeBaseList(KnowledgeBaseListRequest request) {
        return success();
    }

    @PostMapping
    @Operation(summary = "添加知识库")
    public AjaxResult<Void> addKnowledgeBase(@RequestBody KnowledgeBaseAddRequest request) {
        return success();
    }

    @PutMapping
    @Operation(summary = "更新知识库")
    public AjaxResult<Void> updateKnowledgeBase(@RequestBody KnowledgeBaseUpdateRequest request) {
        return success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除知识库")
    public AjaxResult<Void> deleteKnowledgeBase(@PathVariable("id") Integer id) {
        return success();
    }


}
