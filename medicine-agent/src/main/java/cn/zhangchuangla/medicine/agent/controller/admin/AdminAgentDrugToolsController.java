package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
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
 * Admin 端智能体药品工具控制器。
 * <p>
 * 提供给管理端智能体使用的药品详情查询工具接口，
 * 用于查询商品的药品说明书等详细信息。
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/agent/drug")
@Tag(name = "Admin智能体药品工具", description = "用于 Admin 侧智能体药品查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
public class AdminAgentDrugToolsController extends BaseController {

    private final MallProductService agentProductService;

    /**
     * 根据商品 ID 批量查询药品详情。
     * <p>
     * 返回商品的药品详细信息，包括适应症、用法用量、
     * 不良反应、注意事项、禁忌等药品说明书内容。
     *
     * @param productIds 商品 ID 列表，支持批量查询
     * @return 药品详情列表
     */
    @GetMapping("/{productIds}")
    @Operation(summary = "获取药品详情", description = "根据商品ID获取药品详细信息")
    @PreAuthorize("hasAuthority('mall:product:query') or hasRole('super_admin')")
    public AjaxResult<List<AdminAgentDrugDetailVo>> getDrugDetail(
            @Parameter(description = "商品ID")
            @PathVariable List<Long> productIds
    ) {
        return success(agentProductService.getDrugDetail(productIds));
    }
}
