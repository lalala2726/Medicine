package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.annotation.ToolCallStage;
import cn.zhangchuangla.medicine.llm.model.enums.*;
import cn.zhangchuangla.medicine.llm.model.response.ChatResponse;
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
 * 面向大模型的客户端咨询工具
 * <p>
 */
@Component
@RequiredArgsConstructor
public class ClientConsultationTools {

    private final ClientDataProviderLoader providerLoader;
    private final SseMessageInjector messageInjector;

    private ClientDataProvider requireProvider() {
        return providerLoader.getProvider()
                .orElseThrow(() -> new IllegalStateException("未发现 client SPI 提供者，请检查 client 模块是否已注册"));
    }

    /**
     * 搜索商城药品
     */
    @Tool(name = "searchMallProducts", description = """
            <功能>：根据关键字搜索商城内的药品。
            
            <何时使用>：
            1. 当用户描述身体不适（如“头痛”、“发烧”）需要寻找治疗药物时。
            2. 当用户直接询问特定药品名称（如“布洛芬”）时。
            
            <何时禁止使用>：
            - 当用户已经选定商品准备购买时。
            - 当用户只是进行日常闲聊（如打招呼）时。
            """)
    @ToolCallStage(start = "正在搜索商城药品", end = "商城药品搜索完成")
    public List<SearchMallProductTool> searchMallProducts(
            @ToolParam(description = "搜索关键字。可以是具体的症状（如'感冒'）或药品名。") String keyword,
            @ToolParam(description = "最大返回数量，默认为10") int limit) {

        // explanation 参数仅用于引导模型思考，实际逻辑中忽略
        if (keyword == null) {
            return List.of();
        }
        return requireProvider().searchMallProducts(keyword, limit);
    }

    /**
     * 根据药品ID获取药品详细信息
     */
    @Tool(name = "getMallProductById", description = "根据药品ID获取药品详细信息")
    @ToolCallStage(start = "正在查询药品详情", end = "药品详情查询完成")
    public MallProductTool getMallProductById(@ToolParam(description = "药品ID") Long id) {
        if (id == null) {
            return null;
        }
        return requireProvider().getMallProductById(id);
    }

    /**
     * 发送症状选择器
     */
    @Tool(name = "sendSymptomSelector", description = """
            <功能>：发送一个交互式症状选择卡片供用户点击。
            
            <何时使用>：
            - <关键>：当用户描述了一个笼统的病情（如“我发烧了”），你需要进一步确认具体表现（如“体温多少？”、“有无乏力？”）时，**必须优先使用此工具**，而不是让用户打字。
            
            <目标>：
            - 获取结构化的症状信息，以便进行更精准的药品推荐。
            """)
    public String sendSymptomSelector(
            @ToolParam(description = "<思维链>：解释为什么需要细化症状。") String explanation,
            @ToolParam(description = "根据用户初步描述生成的候选症状列表，例如 ['38度以上', '畏寒', '咽喉痛']。") List<String> symptoms,
            @ToolParam(description = "卡片标题，例如'请选择您的具体症状'。") String title) {

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

        ChatResponse response = ChatResponse.builder()
                .role(MessageRole.ASSISTANT)
                .type(MessageType.CARD)
                .card(List.of(payload))
                .build();
        messageInjector.send(response, true);
        return "发送成功";
    }

    /**
     * 发送仅展示的商品卡片
     */
    @Tool(name = "sendProductCard", description = """
            <功能>：向用户发送一组药品的展示卡片（只读，不可直接购买）。
            
            <何时使用>：
            - 在调用 'searchMallProducts' 搜索到结果后，用于向用户展示推荐的药品。
            
            <约束>：
            - 此卡片不具备购买功能。如果用户明确表示要购买，请使用 'snedProductPurchaseCard'。
            - 必须传入有效的商品ID列表。
            """)
    public String sendProductCard(
            @ToolParam(description = "商品ID列表") List<Long> productIds,
            @ToolParam(description = "卡片标题，如'为您推荐以下药品'") String title) {

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
                    if (product == null) return null;
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
                .title(title == null || title.isBlank() ? "为您推荐的商品" : title)
                .medicines(items)
                .build();

        ProductCard productCard = new ProductCard();
        productCard.setCardType(CardType.PRODUCT_CARD);
        productCard.setPayload(payload);

        ChatResponse response = ChatResponse.builder()
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
     * 发送可下单的商品购买卡片
     */
    @Tool(name = "snedProductPurchaseCard", description = """
            <功能>：发送带有“立即购买”按钮的结算卡片。
            
            <何时使用>：
            - 仅在用户**明确表达购买意向**时使用（例如：“我要买这个”、“帮我下单”、“来两盒”）。
            
            <参数要求>：
            - 必须准确解析出用户想要的商品ID和对应的数量（quantity）。
            - 如果用户没说数量，默认为 1。
            """)
    public String snedProductPurchaseCard(
            @ToolParam(description = "<思维链>：确认用户是否明确说了'购买'、'下单'等词汇。") String explanation,
            @ToolParam(description = "购买清单：包含商品ID和购买数量。") List<ProductPurchaseCardQuantity> request,
            @ToolParam(description = "卡片标题，例如'请确认订单'") String title,
            @ToolParam(description = "卡片描述，例如'为您生成的购买清单'") String description) {

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
                    if (product == null) return null;
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

        ChatResponse response = ChatResponse.builder()
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

    /**
     * 打开用户订单列表
     */
    @Tool(name = "openUserOrderList", description = """
            <功能>：在前端触发事件，弹出用户的订单选择列表。
            
            <何时使用>：
            - 当用户询问订单相关问题（如“我的快递呢”、“我要退款”），但**没有提供具体的订单号**时。
            - 引导用户手动选择订单。
            """)
    public void openUserOrderList() {
        messageInjector.callToolAction(EventType.TOOL_CALL_START, "正在打开用户订单列表");
        ChatResponse response = ChatResponse.builder()
                .role(MessageRole.ASSISTANT)
                .type(MessageType.ACTION)
                .action(Action.OPEN_USER_ORDER_LIST)
                .build();

        messageInjector.send(response, true);
        messageInjector.callToolAction(EventType.TOOL_CALL_END, "用户订单列表已打开");
    }

    /**
     * 获取订单详情
     */
    @Tool(name = "getOrderDetailByOrderNo", description = """
            <功能>：根据订单号查询详情。
            
            <何时使用>：
            - 当用户提供了具体的订单号（特征：通常以 'o' 开头，如 'o2024...'）时。
            
            <后续操作>：
            - 获取信息后，请简述订单状态，但**不要**直接列出所有敏感隐私信息，除非用户追问。
            """)
    @ToolCallStage(start = "正在查询订单详情", end = "订单详情查询完成")
    public OrderDetailTool getOrderDetailByOrderNo(
            @ToolParam(description = "订单号，通常以 'o' 开头") String orderNo) {
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
