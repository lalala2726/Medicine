package cn.zhangchuangla.medicine.agent.mapping;

import cn.zhangchuangla.medicine.model.enums.*;

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

    /**
     * 商品状态映射。
     */
    public static final String AGENT_PRODUCT_STATUS = "agent.product.status";

    /**
     * 用户性别映射。
     */
    public static final String AGENT_USER_GENDER = "agent.user.gender";

    /**
     * 用户状态映射。
     */
    public static final String AGENT_USER_STATUS = "agent.user.status";

    /**
     * 用户钱包状态映射。
     */
    public static final String AGENT_USER_WALLET_STATUS = "agent.user.wallet.status";

    /**
     * 钱包流水变动类型映射。
     */
    public static final String AGENT_USER_WALLET_CHANGE_TYPE = "agent.user.wallet.changeType";

    /**
     * 售后类型映射。
     */
    public static final String AGENT_AFTER_SALE_TYPE = "agent.afterSale.type";

    /**
     * 售后状态映射。
     */
    public static final String AGENT_AFTER_SALE_STATUS = "agent.afterSale.status";

    /**
     * 售后申请原因映射。
     */
    public static final String AGENT_AFTER_SALE_REASON = "agent.afterSale.reason";

    /**
     * 收货状态映射。
     */
    public static final String AGENT_AFTER_SALE_RECEIVE_STATUS = "agent.afterSale.receiveStatus";

    /**
     * 订单事件类型映射。
     */
    public static final String AGENT_ORDER_EVENT_TYPE = "agent.order.eventType";

    /**
     * 操作方类型映射。
     */
    public static final String AGENT_OPERATOR_TYPE = "agent.operator.type";

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
        dict.put(AGENT_PRODUCT_STATUS, buildProductStatusDict());
        dict.put(AGENT_USER_GENDER, buildUserGenderDict());
        dict.put(AGENT_USER_STATUS, buildUserStatusDict());
        dict.put(AGENT_USER_WALLET_STATUS, buildWalletStatusDict());
        dict.put(AGENT_USER_WALLET_CHANGE_TYPE, buildWalletChangeTypeDict());
        dict.put(AGENT_AFTER_SALE_TYPE, buildAfterSaleTypeDict());
        dict.put(AGENT_AFTER_SALE_STATUS, buildAfterSaleStatusDict());
        dict.put(AGENT_AFTER_SALE_REASON, buildAfterSaleReasonDict());
        dict.put(AGENT_AFTER_SALE_RECEIVE_STATUS, buildReceiveStatusDict());
        dict.put(AGENT_ORDER_EVENT_TYPE, buildOrderEventTypeDict());
        dict.put(AGENT_OPERATOR_TYPE, buildOperatorTypeDict());
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

    private static Map<String, String> buildProductStatusDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("1", "上架");
        mapping.put("0", "下架");
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildUserGenderDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("0", "未知");
        mapping.put("1", "男");
        mapping.put("2", "女");
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildUserStatusDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("0", "正常");
        mapping.put("1", "禁用");
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildWalletStatusDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("0", "正常");
        mapping.put("1", "冻结");
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildWalletChangeTypeDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (WalletChangeTypeEnum changeTypeEnum : WalletChangeTypeEnum.values()) {
            mapping.put(String.valueOf(changeTypeEnum.getCode()), changeTypeEnum.getName());
        }
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildAfterSaleTypeDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (AfterSaleTypeEnum afterSaleTypeEnum : AfterSaleTypeEnum.values()) {
            mapping.put(afterSaleTypeEnum.getType(), afterSaleTypeEnum.getName());
        }
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildAfterSaleStatusDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (AfterSaleStatusEnum afterSaleStatusEnum : AfterSaleStatusEnum.values()) {
            mapping.put(afterSaleStatusEnum.getStatus(), afterSaleStatusEnum.getName());
        }
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildAfterSaleReasonDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (AfterSaleReasonEnum afterSaleReasonEnum : AfterSaleReasonEnum.values()) {
            mapping.put(afterSaleReasonEnum.getReason(), afterSaleReasonEnum.getName());
        }
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildReceiveStatusDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (ReceiveStatusEnum receiveStatusEnum : ReceiveStatusEnum.values()) {
            mapping.put(receiveStatusEnum.getStatus(), receiveStatusEnum.getName());
        }
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildOrderEventTypeDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (OrderEventTypeEnum orderEventTypeEnum : OrderEventTypeEnum.values()) {
            mapping.put(orderEventTypeEnum.getType(), orderEventTypeEnum.getName());
        }
        return Map.copyOf(mapping);
    }

    private static Map<String, String> buildOperatorTypeDict() {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (OperatorTypeEnum operatorTypeEnum : OperatorTypeEnum.values()) {
            mapping.put(operatorTypeEnum.getType(), operatorTypeEnum.getName());
        }
        return Map.copyOf(mapping);
    }
}
