package cn.zhangchuangla.medicine.llm.spi;

import cn.zhangchuangla.medicine.llm.model.tool.client.MallProductTool;
import cn.zhangchuangla.medicine.llm.model.tool.client.OrderDetailTool;
import cn.zhangchuangla.medicine.llm.model.tool.client.SearchMallProductTool;

import java.util.List;

/**
 * 由 client 模块通过 SPI 提供的用户侧数据，供 LLM 生成卡片消息使用。
 */
public interface ClientDataProvider {


    /**
     * 搜索商品。
     *
     * @param keyword 关键词
     * @param limit   限制数量
     * @return 商品列表
     */
    List<SearchMallProductTool> searchMallProducts(String keyword, int limit);

    /**
     * 获取商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    MallProductTool getMallProductById(Long id);

    /**
     * 批量获取商品详情。
     *
     * @param ids 商品 ID 列表
     * @return 商品详情列表
     */
    List<MallProductTool> getMallProductById(List<Long> ids);

    /**
     * 获取订单详情。
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderDetailTool getOrderDetailByOrderNo(String orderNo);
}
