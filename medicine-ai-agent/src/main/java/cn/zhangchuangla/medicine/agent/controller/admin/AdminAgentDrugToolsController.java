package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.config.condition.ConditionalOnAgentSpi;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.spi.AdminProductDataProvider;
import cn.zhangchuangla.medicine.agent.spi.AgentSpiLoader;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin 端智能体药品工具接口。
 */
@RestController
@RequestMapping("/agent/tools/drug")
@Tag(name = "Admin智能体药品工具", description = "用于 Admin 侧智能体药品查询接口")
@ConditionalOnAgentSpi(AdminProductDataProvider.class)
@InternalAgentHeaderTrace
public class AdminAgentDrugToolsController extends BaseController {

    /**
     * 根据商品 ID 查询药品详情。
     */
    @GetMapping("/{productIds}")
    @Operation(summary = "获取药品详情", description = "根据商品ID获取药品详细信息")
    public AjaxResult<List<AdminAgentDrugDetailVo>> getDrugDetail(
            @Parameter(description = "商品ID")
            @PathVariable List<Long> productIds
    ) {
        AdminProductDataProvider provider = AgentSpiLoader.loadSingle(AdminProductDataProvider.class);
        return success(provider.getDrugDetail(productIds));
    }
}
