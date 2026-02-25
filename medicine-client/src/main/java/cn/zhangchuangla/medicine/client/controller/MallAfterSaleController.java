package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.request.*;
import cn.zhangchuangla.medicine.client.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 售后管理
 *
 * @author Chuang
 * created 2025/11/08
 */
@Slf4j
@RestController
@RequestMapping("/mall/order/after_sale")
@RequiredArgsConstructor
@Tag(name = "售后管理", description = "用户端售后申请、取消、查询接口")
public class MallAfterSaleController extends BaseController {

    private final MallAfterSaleService mallAfterSaleService;

    /**
     * 申请售后
     *
     * @param request 申请售后参数
     * @return 售后申请编号
     */
    @PostMapping("/apply")
    @Operation(summary = "申请售后", description = "用户申请售后服务(仅退款/退货退款/换货)")
    public AjaxResult<Void> applyAfterSale(@Validated @RequestBody AfterSaleApplyRequest request) {
        String afterSaleNo = mallAfterSaleService.applyAfterSale(request);
        return success(afterSaleNo);
    }

    /**
     * 申请整单退款
     */
    @PostMapping("/refund/order")
    @Operation(summary = "申请整单退款", description = "用户对整笔订单发起退款申请，系统自动拆分至订单所有商品")
    public AjaxResult<List<String>> applyOrderRefund(@Validated @RequestBody OrderRefundApplyRequest request) {
        List<String> afterSaleNos = mallAfterSaleService.applyOrderRefund(request);
        return success(afterSaleNos);
    }

    /**
     * 取消售后
     *
     * @param request 取消售后参数
     * @return 响应结果
     */
    @PostMapping("/cancel")
    @Operation(summary = "取消售后", description = "用户取消售后申请(仅待审核状态可取消)")
    public AjaxResult<Void> cancelAfterSale(@Validated @RequestBody AfterSaleCancelRequest request) {
        boolean result = mallAfterSaleService.cancelAfterSale(request);
        return toAjax(result);
    }

    /**
     * 再次申请售后
     */
    @PostMapping("/reapply")
    @Operation(summary = "再次申请售后", description = "售后被拒绝后，用户重新发起申请")
    public AjaxResult<Void> reapplyAfterSale(@Validated @RequestBody AfterSaleReapplyRequest request) {
        String afterSaleNo = mallAfterSaleService.reapplyAfterSale(request);
        return success(afterSaleNo);
    }

    /**
     * 查询售后列表
     *
     * @param request 查询参数
     * @return 售后列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询售后列表", description = "查询当前用户的售后申请列表")
    public AjaxResult<TableDataResult> getAfterSaleList(@Validated AfterSaleListRequest request) {
        Page<AfterSaleListVo> page = mallAfterSaleService.getAfterSaleList(request);
        return getTableData(page);
    }

    /**
     * 售后详情
     *
     * @param afterSaleId 售后申请ID
     * @return 售后详情
     */
    @GetMapping("/detail/{afterSaleId}")
    @Operation(summary = "查询售后详情", description = "查询售后申请详情和时间线")
    public AjaxResult<AfterSaleDetailVo> getAfterSaleDetail(
            @Parameter(description = "售后申请ID", required = true)
            @PathVariable Long afterSaleId) {
        AfterSaleDetailVo detail = mallAfterSaleService.getAfterSaleDetail(afterSaleId);
        return success(detail);
    }
}
