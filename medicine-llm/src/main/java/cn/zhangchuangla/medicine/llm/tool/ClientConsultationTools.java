package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.model.response.card.MedicineCardItem;
import cn.zhangchuangla.medicine.llm.model.response.card.ProductCardItem;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProvider;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProviderLoader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 面向大模型的客户端咨询工具，暴露聊天支持的卡片/消息类型及示例，
 * 以及真实商品/订单数据获取能力，便于大模型按需拼装 SSE 返回数据。
 */
@Component
public class ClientConsultationTools {


    private final ClientDataProviderLoader providerLoader;

    public ClientConsultationTools(ClientDataProviderLoader providerLoader) {
        this.providerLoader = providerLoader;
    }


    /**
     * 获取药品推荐列表（真实数据），用于 medicine-recommend 卡片。
     *
     * @param keyword 关键词或症状描述
     * @param limit   最大返回条数，默认 5，最大 20
     * @return 药品卡片条目列表
     */
    @Tool(name = "recommend_medicines_for_card", description = "获取药品推荐列表，包含价格/处方标识/功效等")
    public List<MedicineCardItem> recommendMedicines(
            @ToolParam(description = "关键词或症状描述") String keyword,
            @ToolParam(description = "返回最大条数，默认 5") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);
        return requireProvider().recommendMedicines(keyword, safeLimit);
    }

    /**
     * 搜索商品/订单，生成 product-card 所需数据。
     *
     * @param keyword 关键词，如订单号、商品名
     * @param limit   最大返回条数，默认 5，最大 20
     * @return 商品/订单卡片条目列表
     */
    @Tool(name = "search_products_for_card", description = "搜索商品/订单，返回 product-card 所需的条目")
    public List<ProductCardItem> searchProducts(
            @ToolParam(description = "关键词，例如商品名、订单号、症状对应商品") String keyword,
            @ToolParam(description = "返回最大条数，默认 5") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);
        return requireProvider().searchProducts(keyword, safeLimit);
    }

    /**
     * 获取最近订单/常购商品列表。
     *
     * @param limit 最大返回条数，默认 5，最大 20
     * @return 商品/订单卡片条目列表
     */
    @Tool(name = "list_latest_orders_for_card", description = "查询最近订单或常购商品，便于直接生成 ")
    public List<ProductCardItem> latestOrders(
            @ToolParam(description = "返回最大条数，默认 5") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);
        return requireProvider().latestOrders(safeLimit);
    }

    /**
     * 根据ID查询单条商品/订单卡片条目。
     *
     * @param productId 商品或订单ID
     * @return 商品/订单卡片条目
     */
    @Tool(name = "get_product_card_by_id", description = "根据 ID 查询商品或订单卡片条目，用于补充")
    public ProductCardItem productCardById(
            @ToolParam(description = "商品或订单ID") String productId) {
        return requireProvider().findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的商品/订单"));
    }

    private ClientDataProvider requireProvider() {
        return providerLoader.getProvider()
                .orElseThrow(() -> new IllegalStateException("未发现 client SPI 提供者，请检查 client 模块是否已注册"));
    }
}
