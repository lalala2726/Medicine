package cn.zhangchuangla.medicine.agent.mapping;

import cn.zhangchuangla.medicine.model.enums.DeliveryTypeEnum;
import cn.zhangchuangla.medicine.model.enums.OrderStatusEnum;
import cn.zhangchuangla.medicine.model.enums.PayTypeEnum;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agent 编码中文映射注册表。
 */
public final class AgentCodeLabelRegistry {

    /**
     * 订单状态编码映射。
     */
    public static final String AGENT_ORDER_STATUS = "agent.order.status";

    /**
     * 支付方式编码映射。
     */
    public static final String AGENT_ORDER_PAY_TYPE = "agent.order.payType";

    /**
     * 商品配送方式编码映射（兼容 legacy 整型编码）。
     */
    public static final String AGENT_PRODUCT_DELIVERY_TYPE = "agent.product.deliveryType";

    private static final Map<String, Map<String, String>> DICT = buildDict();

    private AgentCodeLabelRegistry() {
    }

    /**
     * 根据字典 key 与编码查询中文标签。
     *
     * @param dictKey 字典 key
     * @param code    编码值
     * @return 中文标签，未命中返回 null
     */
    public static String getLabel(String dictKey, String code) {
        if (dictKey == null || dictKey.isBlank() || code == null) {
            return null;
        }
        Map<String, String> mapping = DICT.get(dictKey);
        if (mapping == null) {
            return null;
        }
        return mapping.get(code);
    }

    private static Map<String, Map<String, String>> buildDict() {
        Map<String, Map<String, String>> dict = new LinkedHashMap<>();
        dict.put(AGENT_ORDER_STATUS, buildOrderStatusDict());
        dict.put(AGENT_ORDER_PAY_TYPE, buildPayTypeDict());
        dict.put(AGENT_PRODUCT_DELIVERY_TYPE, buildLegacyDeliveryTypeDict());
        return Map.copyOf(dict);
    }

    private static Map<String, String> buildOrderStatusDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
            mapping.put(statusEnum.getType(), statusEnum.getName());
        }
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildPayTypeDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (PayTypeEnum payTypeEnum : PayTypeEnum.values()) {
            mapping.put(payTypeEnum.getType(), payTypeEnum.getDescription());
        }
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildLegacyDeliveryTypeDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (DeliveryTypeEnum deliveryTypeEnum : DeliveryTypeEnum.values()) {
            mapping.put(String.valueOf(deliveryTypeEnum.ordinal()), deliveryTypeEnum.getName());
        }
        return Map.copyOf(mapping);
    }
}
