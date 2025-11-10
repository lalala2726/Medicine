package cn.zhangchuangla.medicine.client.model.vo;

import cn.zhangchuangla.medicine.model.enums.OrderItemAfterSaleStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 客户端订单列表VO
 *
 * @author Chuang
 * created 2025/11/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单列表VO")
public class OrderListVo {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "订单状态")
    private String orderStatus;

    @Schema(description = "订单状态名称")
    private String orderStatusName;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "实际支付金额")
    private BigDecimal payAmount;

    @Schema(description = "是否已支付(0-否, 1-是)")
    private Integer paid;

    @Schema(description = "是否存在售后")
    private OrderItemAfterSaleStatusEnum afterSaleFlag;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "支付时间")
    private Date payTime;

    @Schema(description = "订单商品列表(只包含必要信息)")
    private List<OrderItemSimpleVo> items;

    /**
     * 订单项简化信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "订单项简化信息")
    public static class OrderItemSimpleVo {

        @Schema(description = "订单项ID")
        private Long id;

        @Schema(description = "商品ID")
        private Long productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品图片")
        private String imageUrl;

        @Schema(description = "购买数量")
        private Integer quantity;

        @Schema(description = "单价")
        private BigDecimal price;

        @Schema(description = "小计金额")
        private BigDecimal totalPrice;

        @Schema(description = "售后状态")
        private String afterSaleStatus;

        @Schema(description = "售后状态名称")
        private String afterSaleStatusName;
    }
}

