package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseDocumentVo;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseListVo;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseVo;
import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.KnowledgeBase;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库管理控制器
 * <p>
 * 提供知识库的增删改查功能，包括列表查询、详情查询、添加、更新和删除操作
 *
 * @author Chuang
 * <p>
 * created on 2025/12/4
 */
@RestController
@RequestMapping("/knowledge_base")
@RequiredArgsConstructor
@IsAdmin
@Tag(name = "知识库管理", description = "知识库 CRUD 接口")
public class KnowledgeBaseController extends BaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 获取知识库列表
     * <p>
     * 根据查询条件分页获取知识库列表信息
     *
     * @param request 知识库列表查询请求参数，包含分页信息和查询条件
     * @return 包含知识库列表的分页数据结果
     */
    @GetMapping("/list")
    @Operation(summary = "知识库列表")
    public AjaxResult<TableDataResult> knowledgeBaseList(KnowledgeBaseListRequest request) {
        Page<KnowledgeBaseListVo> page = knowledgeBaseService.listKnowledgeBase(request);
        return getTableData(page);
    }

    /**
     * 获取知识库详情
     * <p>
     * 根据知识库ID获取指定知识库的详细信息
     *
     * @param id 知识库ID，必须为数字类型
     * @return 包含知识库详细信息的响应对象
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "知识库详情")
    public AjaxResult<KnowledgeBaseVo> knowledgeBaseDetail(@PathVariable("id") Integer id) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getKnowledgeBase(id);
        KnowledgeBaseVo vo = copyProperties(knowledgeBase, KnowledgeBaseVo.class);
        return success(vo);
    }

    /**
     * 添加知识库
     * <p>
     * 创建一个新的知识库记录
     *
     * @param request 知识库添加请求参数，包含知识库的基本信息，经过校验
     * @return 添加操作的结果
     */
    @PostMapping
    @Operation(summary = "添加知识库")
    public AjaxResult<Void> addKnowledgeBase(@Validated @RequestBody KnowledgeBaseAddRequest request) {
        boolean result = knowledgeBaseService.addKnowledgeBase(request);
        return toAjax(result);
    }

    /**
     * 更新知识库
     * <p>
     * 根据知识库ID更新指定知识库的信息
     *
     * @param request 知识库更新请求参数，包含要更新的知识库信息，经过校验
     * @return 更新操作的结果
     */
    @PutMapping
    @Operation(summary = "更新知识库")
    public AjaxResult<Void> updateKnowledgeBase(@Validated @RequestBody KnowledgeBaseUpdateRequest request) {
        boolean result = knowledgeBaseService.updateKnowledgeBase(request);
        return toAjax(result);
    }

    /**
     * 删除知识库
     * <p>
     * 根据知识库ID删除指定的知识库记录
     *
     * @param id 知识库ID，必须为数字类型
     * @return 删除操作的结果
     */
    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "删除知识库")
    public AjaxResult<Void> deleteKnowledgeBase(@PathVariable("id") Integer id) {
        boolean result = knowledgeBaseService.deleteKnowledgeBase(id);
        return toAjax(result);
    }

    /**
     * 导入知识库
     * <p>
     * 导入知识库文件，并解析为知识库记录
     *
     * @param request 导入请求参数，包含导入的文件信息
     * @return 导入操作的结果
     */
    @PostMapping("/document/import")
    @Operation(summary = "导入文档到知识库")
    public AjaxResult<Void> importKnowledgeBase(@Validated @RequestBody KnowledgeBaseImportRequest request) {
        boolean result = knowledgeBaseService.importKnowledgeBase(request);
        return toAjax(result);
    }


    /**
     * 从知识库中删除文档
     * <p>
     * 根据文档ID从指定的知识库中删除文档
     *
     * @param request 文档删除请求参数，包含文档ID
     * @return 删除操作的结果
     */
    @DeleteMapping("/document")
    @Operation(summary = "从知识库删除文档")
    public AjaxResult<Void> deleteDocument(@Validated @RequestBody DocumentDeleteRequest request) {
        boolean result = knowledgeBaseService.deleteDocument(request);
        return toAjax(result);
    }

    /**
     * 获取知识库文档列表
     * <p>
     * 根据知识库ID获取该知识库下的文档列表
     *
     * @param id 知识库ID，必须为数字类型
     * @return 包含文档列表的分页数据结果
     */
    @GetMapping("/{id}/document/list")
    @Operation(summary = "知识库文档列表")
    public AjaxResult<TableDataResult> documentList(@PathVariable("id") Integer id, DocumentListRequest request) {
        Page<KnowledgeBaseDocumentVo> page = knowledgeBaseService.documentList(id, request);
        return getTableData(page);
    }
}
