package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseListVo;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseVo;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@Tag(name = "知识库管理", description = "知识库管理接口")
public class KnowledgeBaseController extends BaseController {

    private final KbBaseService kbBaseService;

    /**
     * 查询知识库列表
     *
     * @param request 查询参数
     * @return 列表分页
     */
    @GetMapping("/list")
    @Operation(summary = "知识库列表")
    @PreAuthorize("hasAuthority('system:knowledge_base:list') or hasRole('super_admin')")
    public AjaxResult<TableDataResult> listKnowledgeBase(KnowledgeBaseListRequest request) {
        Page<KbBase> page = kbBaseService.listKnowledgeBase(request);
        List<KnowledgeBaseListVo> rows = copyListProperties(page, KnowledgeBaseListVo.class);
        return getTableData(page, rows);
    }

    /**
     * 查询知识库详情
     *
     * @param id 主键ID
     * @return 知识库详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "知识库详情")
    @PreAuthorize("hasAuthority('system:knowledge_base:query') or hasRole('super_admin')")
    public AjaxResult<KnowledgeBaseVo> getKnowledgeBaseById(@PathVariable Long id) {
        KbBase kbBase = kbBaseService.getKnowledgeBaseById(id);
        KnowledgeBaseVo vo = copyProperties(kbBase, KnowledgeBaseVo.class);
        return success(vo);
    }

    /**
     * 添加知识库
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加知识库")
    @PreAuthorize("hasAuthority('system:knowledge_base:add') or hasRole('super_admin')")
    public AjaxResult<Void> addKnowledgeBase(@Validated @RequestBody KnowledgeBaseAddRequest request) {
        boolean result = kbBaseService.addKnowledgeBase(request);
        return toAjax(result);
    }

    /**
     * 修改知识库
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改知识库")
    @PreAuthorize("hasAuthority('system:knowledge_base:update') or hasRole('super_admin')")
    public AjaxResult<Void> updateKnowledgeBase(@Validated @RequestBody KnowledgeBaseUpdateRequest request) {
        boolean result = kbBaseService.updateKnowledgeBase(request);
        return toAjax(result);
    }

    /**
     * 启用知识库
     *
     * @param id 主键ID
     * @return 启用结果
     */
    @PostMapping("/{id:\\d+}/enable")
    @Operation(summary = "启用知识库")
    @PreAuthorize("hasAuthority('system:knowledge_base:enable') or hasRole('super_admin')")
    public AjaxResult<Void> enableKnowledgeBase(@PathVariable Long id) {
        boolean result = kbBaseService.enableKnowledgeBase(id);
        return toAjax(result);
    }

    /**
     * 禁用知识库
     *
     * @param id 主键ID
     * @return 禁用结果
     */
    @PostMapping("/{id:\\d+}/disable")
    @Operation(summary = "禁用知识库")
    @PreAuthorize("hasAuthority('system:knowledge_base:disable') or hasRole('super_admin')")
    public AjaxResult<Void> disableKnowledgeBase(@PathVariable Long id) {
        boolean result = kbBaseService.disableKnowledgeBase(id);
        return toAjax(result);
    }

    /**
     * 删除知识库
     *
     * @param ids 主键ID集合
     * @return 删除结果
     */
    @DeleteMapping("/{ids:\\d+(?:,\\d+)*}")
    @Operation(summary = "删除知识库")
    @PreAuthorize("hasAuthority('system:knowledge_base:delete') or hasRole('super_admin')")
    public AjaxResult<Void> deleteKnowledgeBase(@PathVariable List<Long> ids) {
        boolean result = kbBaseService.deleteKnowledgeBase(ids);
        return toAjax(result);
    }

}
