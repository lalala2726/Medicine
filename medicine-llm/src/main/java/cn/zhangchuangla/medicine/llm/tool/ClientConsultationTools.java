package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import cn.zhangchuangla.medicine.llm.model.enums.EventType;
import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.enums.MessageType;
import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import cn.zhangchuangla.medicine.llm.model.response.ProductCard;
import cn.zhangchuangla.medicine.llm.model.response.ProductPurchaseCard;
import cn.zhangchuangla.medicine.llm.model.response.SymptomSelectorCard;
import cn.zhangchuangla.medicine.llm.model.tool.client.MallProductTool;
import cn.zhangchuangla.medicine.llm.model.tool.client.MedicineCardItemTool;
import cn.zhangchuangla.medicine.llm.model.tool.client.OrderDetailTool;
import cn.zhangchuangla.medicine.llm.model.tool.client.SearchMallProductTool;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProvider;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProviderLoader;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public List<SearchMallProductTool> searchMallProducts(@ToolParam(description = "搜索关键字,可以搜索商品(药品)可以搜索生病的症状") String keyword,
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
    public MallProductTool getMallProductById(@ToolParam(description = "药品ID") Long id) {
        if (id == null) {
            return null;
        }
        return requireProvider().getMallProductById(id);
    }

    /**
     * 发送仅展示的商品卡片，不包含购买交互。
     *
     * @param productIds 商品ID列表，去重后按输入顺序展示
     * @param title      卡片标题，空则使用默认提示
     * @return 发送结果描述，成功或失败原因
     */
    @Tool(name = "sendProductCard", description = """
            向用户发送商品推荐卡片，仅用于展示商品信息（不包含购买操作）。
            适用于用户想了解或比较商品时使用，需传入有效的商品ID列表。
            """)
    public String sendProductCard(@ToolParam(description = "商品ID") List<Long> productIds,
                                  @ToolParam(description = "卡片标题") String title) {
        if (productIds == null || productIds.isEmpty()) {
            return "未发送，商品ID列表为空";
        }

        List<Long> distinctProductIds = productIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctProductIds.isEmpty()) {
            return "未发送，商品ID列表为空";
        }

        List<MallProductTool> products;
        try {
            products = requireProvider().getMallProductById(distinctProductIds);
        } catch (Exception ex) {
            return "发送失败：" + ex.getMessage();
        }
        if (products == null || products.isEmpty()) {
            return "未发送，未查询到对应商品";
        }

        Map<Long, MallProductTool> productMap = products.stream()
                .filter(Objects::nonNull)
                .filter(product -> product.getId() != null)
                .collect(Collectors.toMap(MallProductTool::getId, product -> product, (left, right) -> left));

        List<MedicineCardItemTool> items = distinctProductIds.stream()
                .map(productId -> {
                    MallProductTool product = productMap.get(productId);
                    if (product == null) {
                        return null;
                    }
                    return MedicineCardItemTool.builder()
                            .id(String.valueOf(product.getId()))
                            .name(product.getName())
                            .image(product.getCoverImage())
                            .price(product.getPrice())
                            .spec(product.getUnit())
                            .efficacy(product.getDrugDetail() == null ? null : product.getDrugDetail().getEfficacy())
                            .prescription(product.getDrugDetail() == null ? null : product.getDrugDetail().getPrescription())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        if (items.isEmpty()) {
            return "未发送，未查询到对应商品";
        }

        ProductCard.ProductCardPayload payload = ProductCard.ProductCardPayload.builder()
                .title(title == null || title.isBlank() ? "为你推荐的商品" : title)
                .medicines(items)
                .build();

        ProductCard productCard = new ProductCard();
        productCard.setCardType(CardType.PRODUCT_CARD);
        productCard.setPayload(payload);

        ClientChatResponse response = ClientChatResponse.builder()
                .role(MessageRole.ASSISTANT)
                .type(MessageType.CARD)
                .card(List.of(productCard))
                .build();

        try {
            messageInjector.send(response, true);
            return "发送成功";
        } catch (Exception ex) {
            return "发送失败：" + ex.getMessage();
        }
    }

    /**
     * 发送可下单的商品购买卡片，支持携带商品数量。
     *
     * @param request     商品ID与数量列表，数量<=0时自动按1处理并按ID聚合求和
     * @param title       卡片标题，空则使用默认提示
     * @param description 卡片描述，空则使用默认提示
     * @return 发送结果描述，成功或失败原因
     */
    @Tool(name = "snedProductPurchaseCard", description = """
            为用户发送商品购买卡片，用户可直接点击购买。
            仅在确认用户有购买需求时使用，商品数量至少为 1。
            返回发送结果或失败原因。
            """)
    public String snedProductPurchaseCard(@ToolParam(description = "商品ID和商品数量") List<ProductPurchaseCardQuantity> request,
                                          @ToolParam(description = "卡片标题") String title,
                                          @ToolParam(description = "卡片描述") String description) {
        if (request == null || request.isEmpty()) {
            return "未发送，商品参数为空";
        }

        Map<Long, Integer> quantityByProductId = new LinkedHashMap<>();
        for (ProductPurchaseCardQuantity item : request) {
            if (item == null || item.getProductId() == null) {
                continue;
            }
            Integer quantity = item.getQuantity();
            int safeQuantity = quantity == null || quantity <= 0 ? 1 : quantity;
            quantityByProductId.merge(item.getProductId(), safeQuantity, Integer::sum);
        }

        if (quantityByProductId.isEmpty()) {
            return "未发送，商品参数为空";
        }

        List<Long> productIds = quantityByProductId.keySet().stream().toList();

        List<MallProductTool> products;
        try {
            products = requireProvider().getMallProductById(productIds);
        } catch (Exception ex) {
            return "发送失败：" + ex.getMessage();
        }
        if (products == null || products.isEmpty()) {
            return "未发送，未查询到对应商品";
        }

        Map<Long, MallProductTool> productMap = products.stream()
                .filter(Objects::nonNull)
                .filter(product -> product.getId() != null)
                .collect(Collectors.toMap(MallProductTool::getId, product -> product, (left, right) -> left));

        List<MedicineCardItemTool> items = productIds.stream()
                .map(productId -> {
                    MallProductTool product = productMap.get(productId);
                    if (product == null) {
                        return null;
                    }
                    return MedicineCardItemTool.builder()
                            .id(String.valueOf(product.getId()))
                            .name(product.getName())
                            .image(product.getCoverImage())
                            .price(product.getPrice())
                            .spec(product.getUnit())
                            .efficacy(product.getDrugDetail() == null ? null : product.getDrugDetail().getEfficacy())
                            .prescription(product.getDrugDetail() == null ? null : product.getDrugDetail().getPrescription())
                            .quantity(quantityByProductId.get(productId))
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        if (items.isEmpty()) {
            return "未发送，未查询到对应商品";
        }

        ProductPurchaseCard.ProductPurchaseCardPayload payload = ProductPurchaseCard.ProductPurchaseCardPayload.builder()
                .title(title == null || title.isBlank() ? "下单推荐" : title)
                .description(description == null || description.isBlank() ? "为你准备的购买清单" : description)
                .medicines(items)
                .build();

        ClientChatResponse response = ClientChatResponse.builder()
                .role(MessageRole.ASSISTANT)
                .type(MessageType.CARD)
                .card(List.of(ProductPurchaseCard.builder()
                        .cardType(CardType.PRODUCT_PURCHASE)
                        .payload(payload)
                        .build()))
                .build();
        try {
            messageInjector.send(response, true);
            return "发送成功";
        } catch (Exception ex) {
            return "发送失败：" + ex.getMessage();
        }
    }

    @Tool(name = "openUserOrderList", description = """
            调用此用户会发送一个打开用户前端订单列表的参数,让用户选择订单
            """)
    public void openUserOrderList() {
        ClientChatResponse response = ClientChatResponse.builder()
                .role(MessageRole.ASSISTANT)
                .type(MessageType.EVENT)
                .event(EventType.OPEN_USER_ORDER_LIST)
                .build();

        messageInjector.send(response, true);
    }


    @Tool(name = "sendSymptomSelector", description = """
            当需要询问用户有哪些症状的时候,需要进行选择的时候这边可以提供相关的选择让用户进行选择,减少用户的操作
            """)
    public String sendSymptomSelector(@ToolParam(description = "症状列表") List<String> symptoms, @ToolParam(description = "标题") String title) {
        if (symptoms == null || symptoms.isEmpty()) {
            return "未发送，症状参数为空";
        }
        if (title == null || title.isBlank()) {
            title = "请选择症状";
        }
        SymptomSelectorCard payload = SymptomSelectorCard.builder()
                .title(title)
                .options(symptoms)
                .build();

        ClientChatResponse response = ClientChatResponse.builder()
                .role(MessageRole.ASSISTANT)
                .type(MessageType.CARD)
                .card(List.of(payload))
                .build();
        messageInjector.send(response, true);
        return "发送成功";
    }

    @Tool(name = "getOrderDetailByOrderNo", description = """
            根据订单号获取订单详情
            """)
    public OrderDetailTool getOrderDetailByOrderNo(@ToolParam(description = "订单号") String orderNo) {
        return requireProvider().getOrderDetailByOrderNo(orderNo);
    }

    @Data
    public static class ProductPurchaseCardQuantity {

        @ToolParam(description = "商品ID")
        private Long productId;

        @ToolParam(description = "商品数量")
        private Integer quantity;
    }
}
