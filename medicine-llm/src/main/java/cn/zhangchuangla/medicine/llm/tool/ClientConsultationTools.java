package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.enums.MessageType;
import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import cn.zhangchuangla.medicine.llm.model.response.card.MedicineRecommendCard;
import cn.zhangchuangla.medicine.llm.model.tool.ClientMallProductOut;
import cn.zhangchuangla.medicine.llm.model.tool.ClientSearchMallProductOut;
import cn.zhangchuangla.medicine.llm.model.tool.MedicineCardItem;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProvider;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProviderLoader;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 面向大模型的客户端咨询工具，暴露聊天支持的卡片/消息类型及示例，
 * 以及真实商品/订单数据获取能力
 */
@Component
@RequiredArgsConstructor
public class ClientConsultationTools {


    private ClientDataProvider requireProvider() {
        return providerLoader.getProvider()
                .orElseThrow(() -> new IllegalStateException("未发现 client SPI 提供者，请检查 client 模块是否已注册"));
    }

    private final ClientDataProviderLoader providerLoader;
    private final SseMessageInjector messageInjector;


    /**
     * 搜索商城药品,用于当AI诊断完病情之后获取药品信息
     *
     * @param keyword 关键字
     * @param limit   最大返回数量
     * @return 商品列表
     */
    @Tool(name = "searchMallProducts", description = "搜索药品信息")
    public List<ClientSearchMallProductOut> searchMallProducts(@ToolParam(description = "搜索关键字,可以搜索商品(药品)可以搜索生病的症状") String keyword,
                                                               @ToolParam(description = "最大返回数量,默认为10") int limit) {
        if (keyword == null) {
            return List.of();
        }
        return requireProvider().searchMallProducts(keyword, limit);
    }

    /**
     * 根据药品ID获取药品详细信息
     *
     * @param id 商品ID
     * @return 商品详细信息
     */
    @Tool(name = "getMallProductById", description = "根据药品ID获取药品详细信息")
    public ClientMallProductOut getMallProductById(@ToolParam(description = "药品ID") Long id) {
        if (id == null) {
            return null;
        }
        return requireProvider().getMallProductById(id);
    }

    @Tool(name = "snedProductCardMessage", description = "调用此工具传递相关的商品ID将会给用户聊天窗口发送商品卡片")
    public void snedProductCardMessage(@ToolParam(description = "商品ID") List<Long> productIds,
                                       @ToolParam(description = "卡片标题") String title,
                                       @ToolParam(description = "卡片描述") String description) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }

        List<ClientMallProductOut> products = requireProvider().getMallProductById(productIds);
        if (products == null || products.isEmpty()) {
            return;
        }

        List<MedicineCardItem> items = products.stream()
                .filter(Objects::nonNull)
                .map(product -> MedicineCardItem.builder()
                        .id(String.valueOf(product.getId()))
                        .name(product.getName())
                        .image(product.getCoverImage())
                        .price(product.getPrice())
                        .spec(product.getUnit())
                        .efficacy(product.getDrugDetail() == null ? null : product.getDrugDetail().getEfficacy())
                        .prescription(product.getDrugDetail() == null ? null : product.getDrugDetail().getPrescription())
                        .build())
                .toList();

        MedicineRecommendCard.MedicineRecommendPayload payload = MedicineRecommendCard.MedicineRecommendPayload.builder()
                .title(title == null ? "药品推荐" : title)
                .description(description == null ? "相关药品推荐" : description)
                .medicines(items)
                .build();

        ClientChatResponse response = ClientChatResponse.builder()
                .role(MessageRole.ASSISTANT)
                .type(MessageType.CARD)
                .card(List.of(MedicineRecommendCard.builder()
                        .cardType(CardType.PRODUCT_PURCHASE)
                        .payload(payload)
                        .build()))
                .build();
        messageInjector.send(response, true);
    }

    private List<String> buildTags(ClientMallProductOut product) {
        return Stream.of(product.getCategoryName(), product.getUnit())
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toList());
    }
}
