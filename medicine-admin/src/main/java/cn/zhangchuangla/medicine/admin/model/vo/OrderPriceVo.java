package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单价格信息VO
 *
 * @author Chuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单价格信息")
public class OrderPriceVo {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    private Long orderId;

    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    /**
     * 实际支付金额
     */
    @Schema(description = "实际支付金额")
    private BigDecimal payAmount;

}

