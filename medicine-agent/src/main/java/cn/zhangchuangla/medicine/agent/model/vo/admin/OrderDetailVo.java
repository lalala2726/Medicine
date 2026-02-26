package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentFieldDesc;
import cn.zhangchuangla.medicine.agent.annotation.AgentVoDesc;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import cn.zhangchuangla.medicine.common.core.annotation.DataMasking;
import cn.zhangchuangla.medicine.common.core.enums.MaskingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端智能体订单详情。
 */
@Schema(description = "管理端智能体订单详情")
@AgentVoDesc("管理端智能体订单详情")
@Data
public class OrderDetailVo {

    @Schema(description = "用户信息")
    @AgentFieldDesc("用户信息")
    private UserInfo userInfo;

    @Schema(description = "配送信息")
    @AgentFieldDesc("配送信息")
    private DeliveryInfo deliveryInfo;

    @Schema(description = "订单信息")
    @AgentFieldDesc("订单信息")
    private OrderInfo orderInfo;

    @Schema(description = "商品信息")
    @AgentFieldDesc("商品信息")
    private List<ProductInfo> productInfo;

    @Data
    @AgentVoDesc("用户信息")
    public static class UserInfo {

        @Schema(description = "用户ID")
        @AgentFieldDesc("用户ID")
        private String userId;

        @Schema(description = "用户昵称")
        @AgentFieldDesc("用户昵称")
        private String nickname;

        @Schema(description = "用户手机号")
        @AgentFieldDesc("用户手机号")
        @DataMasking(type = MaskingType.MOBILE_PHONE)
        private String phoneNumber;
    }

    @Data
    @AgentVoDesc("配送信息")
    public static class DeliveryInfo {

        @Schema(description = "收货人")
        @AgentFieldDesc("收货人")
        private String receiverName;

        @Schema(description = "收货地址")
        @AgentFieldDesc("收货地址")
        private String receiverAddress;

        @Schema(description = "收货人电话")
        @AgentFieldDesc("收货人电话")
        private String receiverPhone;

        @Schema(description = "配送方式")
        @AgentFieldDesc("配送方式")
        private String deliveryMethod;
    }

    @Data
    @AgentVoDesc("订单信息")
    public static class OrderInfo {

        @Schema(description = "订单编号")
        @AgentFieldDesc("订单编号")
        private String orderNo;

        @Schema(description = "订单状态")
        @AgentFieldDesc("订单状态")
        @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_ORDER_STATUS)
        private String orderStatus;

        @Schema(description = "支付方式")
        @AgentFieldDesc("支付方式")
        @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_ORDER_PAY_TYPE)
        private String payType;

        @Schema(description = "订单总金额")
        @AgentFieldDesc("订单总金额")
        private BigDecimal totalAmount;

        @Schema(description = "实际支付金额")
        @AgentFieldDesc("实际支付金额")
        private BigDecimal payAmount;

        @Schema(description = "运费金额")
        @AgentFieldDesc("运费金额")
        private BigDecimal freightAmount;
    }

    @Data
    @AgentVoDesc("商品信息")
    public static class ProductInfo {

        @Schema(description = "商品ID")
        @AgentFieldDesc("商品ID")
        private Long productId;

        @Schema(description = "商品名称")
        @AgentFieldDesc("商品名称")
        private String productName;

        @Schema(description = "商品图片")
        @AgentFieldDesc("商品图片")
        private String productImage;

        @Schema(description = "商品价格")
        @AgentFieldDesc("商品价格")
        private BigDecimal productPrice;

        @Schema(description = "商品数量")
        @AgentFieldDesc("商品数量")
        private Integer productQuantity;

        @Schema(description = "商品总价")
        @AgentFieldDesc("商品总价")
        private BigDecimal productTotalAmount;
    }
}
