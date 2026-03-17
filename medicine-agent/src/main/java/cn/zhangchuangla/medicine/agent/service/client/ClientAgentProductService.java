package cn.zhangchuangla.medicine.agent.service.client;

import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 客户端智能体商品服务接口。
 */
public interface ClientAgentProductService {

    /**
     * 搜索商品。
     *
     * @param request 搜索参数
     * @return 分页结果
     */
    Page<ClientAgentProductSearchDto> searchProducts(ClientAgentProductSearchRequest request);

    /**
     * 查询商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    MallProductDetailDto getProductDetail(Long productId);

    /**
     * 查询商品卡片补全结果。
     *
     * @param productIds 商品ID列表
     * @return 商品卡片补全结果
     */
    ClientAgentProductCardsDto getProductCards(List<Long> productIds);

    /**
     * 查询商品购买卡片结果。
     *
     * @param items 商品购买项列表
     * @return 商品购买卡片结果
     */
    ClientAgentProductPurchaseCardsDto getProductPurchaseCards(List<ClientAgentProductPurchaseQueryDto> items);

    /**
     * 查询商品规格属性。
     *
     * @param productId 商品ID
     * @return 商品规格属性
     */
    ClientAgentProductSpecDto getProductSpec(Long productId);
}
