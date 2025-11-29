package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import cn.zhangchuangla.medicine.llm.model.response.card.MedicineCardItem;
import cn.zhangchuangla.medicine.llm.model.response.card.ProductCardItem;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProvider;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProviderLoader;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
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
    private final SseMessageInjector messageInjector;

    public ClientConsultationTools(ClientDataProviderLoader providerLoader, SseMessageInjector messageInjector) {
        this.providerLoader = providerLoader;
        this.messageInjector = messageInjector;
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
        messageInjector.send(buildNotice("正在调用 recommend_medicines_for_card 工具，为您查询药品推荐..."));
        return requireProvider().recommendMedicines(keyword, safeLimit);
    }

    /**
     * 获取最近订单/常购商品列表。
     *
     * @param limit 最大返回条数，默认 5，最大 20
     * @return 商品/订单卡片条目列表
     */
    @Tool(name = "list_latest_orders_for_card", description = "查询最近订单或常购商品，便于直接生成 ")
    public List<ProductCardItem> latestOrders(
            @ToolParam(description = "返回最大条数，默认 5") Integer limit,
            @ToolParam(description = """
                    是否直接发送订单信息给用户。若为 true，系统将直接发送卡片消息此工具返回 null；
                    若为 false 或不传入，则返回订单数据供 AI 分析使用
                    """) Boolean isSned) {
        int safeLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);
        messageInjector.send(buildNotice("正在调用 list_latest_orders_for_card 工具，查询最近订单..."));
        return requireProvider().latestOrders(safeLimit);
    }


    private ClientChatResponse buildNotice(String content) {
        ClientChatResponse resp = new ClientChatResponse();
        resp.setRole(MessageRole.ASSISTANT);
        resp.setContent(content);
        return resp;
    }

    private ClientDataProvider requireProvider() {
        return providerLoader.getProvider()
                .orElseThrow(() -> new IllegalStateException("未发现 client SPI 提供者，请检查 client 模块是否已注册"));
    }
}
