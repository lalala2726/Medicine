package cn.zhangchuangla.medicine.agent.controller.client;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.vo.client.ClientAgentProductPurchaseCardsVo;
import cn.zhangchuangla.medicine.agent.service.client.ClientAgentProductService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductPurchaseCardsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 客户端智能体商品卡片控制器。
 */
@RestController
@RequestMapping("/agent/client/purchase_cards")
@Tag(name = "客户端智能体商品卡片工具", description = "用于客户端智能体商品卡片补全接口")
@InternalAgentHeaderTrace
@Validated
@RequiredArgsConstructor
public class AgentClientProductCardController extends BaseController {

    /**
     * 客户端智能体商品服务。
     */
    private final ClientAgentProductService clientAgentProductService;

    /**
     * 根据商品ID列表查询商品购买卡片补全结果。
     *
     * @param productIds 商品ID列表
     * @return 商品购买卡片补全结果
     */
    @GetMapping("/{productIds}")
    @Operation(summary = "获取商品购买卡片", description = "根据商品ID列表获取客户端智能体商品购买卡片补全结果")
    public AjaxResult<ClientAgentProductPurchaseCardsVo> getProductPurchaseCards(@PathVariable List<Long> productIds) {
        ClientAgentProductPurchaseCardsDto cards = clientAgentProductService.getProductPurchaseCards(productIds);
        ClientAgentProductPurchaseCardsVo target = copyProperties(cards, ClientAgentProductPurchaseCardsVo.class);
        target.setItems(copyListProperties(cards.getItems(), ClientAgentProductPurchaseCardsVo.ClientAgentProductPurchaseItemVo.class));
        return success(target);
    }
}
