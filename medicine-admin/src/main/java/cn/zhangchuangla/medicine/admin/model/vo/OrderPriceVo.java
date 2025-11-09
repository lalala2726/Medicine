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

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单号", example = "ORDER20230725001")
    private String orderNo;

    @Schema(description = "订单总金额", example = "99.99")
    private BigDecimal totalAmount;

}

