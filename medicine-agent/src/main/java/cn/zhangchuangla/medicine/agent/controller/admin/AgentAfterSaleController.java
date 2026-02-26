package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AgentAfterSaleDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AgentAfterSaleListVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AgentAfterSaleTimelineVo;
import cn.zhangchuangla.medicine.agent.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端智能体售后工具控制器。
 * <p>
 * 提供给管理端智能体使用的售后查询工具接口，
 * 支持售后列表与售后详情查询。
 */
@RestController
@RequestMapping("/agent/admin/after-sale")
@Tag(name = "管理端智能体售后工具", description = "用于管理端智能体售后查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
public class AgentAfterSaleController extends BaseController {

    private final MallAfterSaleService mallAfterSaleService;

    /**
     * 分页查询售后列表。
     *
     * @param request 查询参数
     * @return 售后分页数据
     */
    @GetMapping("/list")
    @Operation(summary = "售后列表", description = "分页查询售后申请列表")
    @PreAuthorize("hasAuthority('mall:after_sale:list') or hasRole('super_admin')")
    public AjaxResult<TableDataResult> listAfterSales(MallAfterSaleListRequest request) {
        MallAfterSaleListRequest safeRequest = request == null ? new MallAfterSaleListRequest() : request;
        Page<AfterSaleListVo> page = mallAfterSaleService.listAfterSales(safeRequest);
        List<AgentAfterSaleListVo> rows = copyListProperties(page.getRecords(), AgentAfterSaleListVo.class);
        return getTableData(page, rows);
    }

    /**
     * 查询售后详情。
     *
     * @param afterSaleId 售后申请 ID
     * @return 售后详情
     */
    @GetMapping("/detail/{afterSaleId}")
    @Operation(summary = "售后详情", description = "根据售后申请ID查询售后详情")
    @PreAuthorize("hasAuthority('mall:after_sale:query') or hasRole('super_admin')")
    public AjaxResult<AgentAfterSaleDetailVo> getAfterSaleDetail(
            @Parameter(description = "售后申请ID", required = true)
            @PathVariable Long afterSaleId) {
        AfterSaleDetailVo detail = mallAfterSaleService.getAfterSaleDetail(afterSaleId);
        return success(toAgentAfterSaleDetailVo(detail));
    }

    private AgentAfterSaleDetailVo toAgentAfterSaleDetailVo(AfterSaleDetailVo source) {
        if (source == null) {
            return null;
        }
        AgentAfterSaleDetailVo target = copyProperties(source, AgentAfterSaleDetailVo.class);
        target.setProductInfo(copyProperties(source.getProductInfo(), AgentAfterSaleDetailVo.ProductInfo.class));
        target.setTimeline(copyListProperties(source.getTimeline(), AgentAfterSaleTimelineVo.class));
        return target;
    }
}
