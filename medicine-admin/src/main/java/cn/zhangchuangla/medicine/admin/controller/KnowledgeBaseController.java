package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.dto.KnowledgeBaseListDto;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.AgentModelOptionVo;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseListVo;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseVo;
import cn.zhangchuangla.medicine.admin.service.AgentConfigService;
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

import java.util.ArrayList;
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
    private final AgentConfigService agentConfigService;

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
        Page<KnowledgeBaseListDto> page = kbBaseService.listKnowledgeBase(request);
        return getTableData(page, toKnowledgeBaseListVo(page.getRecords()));
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
     * 查询知识库可选向量模型列表。
     *
     * @return 当前激活提供商下的向量模型选项
     */
    @GetMapping("/embedding-model/option")
    @Operation(summary = "知识库向量模型选项")
    @PreAuthorize("hasAuthority('system:knowledge_base:list') or hasRole('super_admin')")
    public AjaxResult<List<AgentModelOptionVo>> listKnowledgeBaseEmbeddingModelOptions() {
        return success(agentConfigService.listEmbeddingModelOptions());
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
     * 删除知识库
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "删除知识库")
    @PreAuthorize("hasAuthority('system:knowledge_base:delete') or hasRole('super_admin')")
    public AjaxResult<Void> deleteKnowledgeBase(@PathVariable Long id) {
        boolean result = kbBaseService.deleteKnowledgeBase(id);
        return toAjax(result);
    }

    private List<KnowledgeBaseListVo> toKnowledgeBaseListVo(List<KnowledgeBaseListDto> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        List<KnowledgeBaseListVo> rows = new ArrayList<>(records.size());
        for (KnowledgeBaseListDto dto : records) {
            if (dto == null) {
                continue;
            }
            rows.add(toKnowledgeBaseListVo(dto));
        }
        return rows;
    }

    private KnowledgeBaseListVo toKnowledgeBaseListVo(KnowledgeBaseListDto dto) {
        KnowledgeBaseListVo vo = new KnowledgeBaseListVo();
        vo.setId(dto.getId());
        vo.setKnowledgeName(dto.getKnowledgeName());
        vo.setDisplayName(dto.getDisplayName());
        vo.setCover(dto.getCover());
        vo.setDescription(dto.getDescription());
        vo.setStatus(dto.getStatus());
        vo.setDetail(toKnowledgeBaseListDetailVo(dto.getDetail()));
        return vo;
    }

    private KnowledgeBaseListVo.Detail toKnowledgeBaseListDetailVo(KnowledgeBaseListDto.Detail detailDto) {
        if (detailDto == null) {
            return null;
        }
        KnowledgeBaseListVo.Detail detailVo = new KnowledgeBaseListVo.Detail();
        detailVo.setUpdateTime(detailDto.getUpdateTime());
        detailVo.setChunkCount(detailDto.getChunkCount());
        detailVo.setFileCount(detailDto.getFileCount());
        return detailVo;
    }

}
