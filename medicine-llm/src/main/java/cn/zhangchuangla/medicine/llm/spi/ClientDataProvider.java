package cn.zhangchuangla.medicine.llm.spi;

import cn.zhangchuangla.medicine.llm.model.response.card.MedicineCardItem;
import cn.zhangchuangla.medicine.llm.model.response.card.ProductCardItem;

import java.util.List;
import java.util.Optional;

/**
 * 由 client 模块通过 SPI 提供的用户侧数据，供 LLM 生成卡片消息使用。
 */
public interface ClientDataProvider {

    /**
     * 按关键词推荐或搜索药品，返回药品卡片项列表。
     */
    List<MedicineCardItem> recommendMedicines(String keyword, int limit);

    /**
     * 搜索或筛选商品/订单，供卡片选择使用。
     */
    List<ProductCardItem> searchProducts(String keyword, int limit);

    /**
     * 查询用户最近的订单/商品列表，按时间倒序。
     */
    List<ProductCardItem> latestOrders(int limit);

    /**
     * 根据ID查询单个商品/订单卡片项。
     */
    Optional<ProductCardItem> findProductById(String productId);
}
