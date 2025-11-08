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
@Schema(description = "订单价格信息", example = "OrderPriceVo")
public class OrderPriceVo {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    /**
     * 订单号
     */
    @Schema(description = "订单号", example = "ORDER20230725001")
    private String orderNo;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额", example = "99.99")
    private BigDecimal totalAmount;

}

