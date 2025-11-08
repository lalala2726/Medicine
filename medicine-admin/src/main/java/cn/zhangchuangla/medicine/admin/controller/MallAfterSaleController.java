package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.AfterSaleAuditRequest;
import cn.zhangchuangla.medicine.admin.model.request.AfterSaleListRequest;
import cn.zhangchuangla.medicine.admin.model.request.AfterSaleProcessRequest;
import cn.zhangchuangla.medicine.admin.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 售后管理Controller(管理端)
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Slf4j
@RestController
@RequestMapping("/mall/after-sale")
@RequiredArgsConstructor
@Tag(name = "售后管理(管理端)", description = "管理端售后审核、处理、查询接口")
public class MallAfterSaleController extends BaseController {

    private final MallAfterSaleService mallAfterSaleService;

    /**
     * 查询售后列表
     *
     * @param request 查询参数
     * @return 售后列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询售后列表", description = "管理员查询所有售后申请列表")
    public AjaxResult<TableDataResult> getAfterSaleList(@Valid AfterSaleListRequest request) {
        Page<AfterSaleListVo> page = mallAfterSaleService.getAfterSaleList(request);
        return getTableData(page);
    }

    /**
     * 获取售后详情
     *
     * @param afterSaleId 售后申请ID
     * @return 售后详情
     */
    @GetMapping("/detail/{afterSaleId}")
    @Operation(summary = "查询售后详情", description = "管理员查询售后申请详情")
    public AjaxResult<AfterSaleDetailVo> getAfterSaleDetail(
            @Parameter(description = "售后申请ID", required = true)
            @PathVariable Long afterSaleId) {
        AfterSaleDetailVo detail = mallAfterSaleService.getAfterSaleDetail(afterSaleId);
        return success(detail);
    }

    /**
     * 审核售后申请
     *
     * @param request 审核参数
     * @return 是否成功
     */
    @PostMapping("/audit")
    @Operation(summary = "审核售后申请", description = "管理员审核售后申请(通过/拒绝)")
    public AjaxResult<Void> auditAfterSale(@Valid @RequestBody AfterSaleAuditRequest request) {
        boolean result = mallAfterSaleService.auditAfterSale(request);
        return toAjax(result);
    }

    /**
     * 处理退款
     *
     * @param request 处理参数
     * @return 是否成功
     */
    @PostMapping("/process-refund")
    @Operation(summary = "处理退款", description = "管理员处理售后退款(原路退回)")
    public AjaxResult<Void> processRefund(@Valid @RequestBody AfterSaleProcessRequest request) {
        boolean result = mallAfterSaleService.processRefund(request);
        return toAjax(result);
    }

    /**
     * 处理换货
     *
     * @param request 处理参数
     * @return 是否成功
     */
    @PostMapping("/process-exchange")
    @Operation(summary = "处理换货", description = "管理员处理售后换货")
    public AjaxResult<Void> processExchange(@Valid @RequestBody AfterSaleProcessRequest request) {
        boolean result = mallAfterSaleService.processExchange(request);
        return toAjax(result);
    }
}

