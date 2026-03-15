package cn.zhangchuangla.medicine.rpc.client;

import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSearchDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSpecDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;

/**
 * 客户端智能体商品只读 RPC。
 */
public interface ClientAgentProductRpcService {

    /**
     * 按关键词分页搜索商品。
     *
     * @param request 商品搜索参数
     * @return 商品分页结果
     */
    PageResult<ClientAgentProductSearchDto> searchProducts(ClientAgentProductSearchRequest request);

    /**
     * 查询商品完整详情与药品说明信息。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    MallProductDetailDto getProductDetail(Long productId);

    /**
     * 查询商品规格属性。
     *
     * @param productId 商品ID
     * @return 商品规格属性
     */
    ClientAgentProductSpecDto getProductSpec(Long productId);
}
