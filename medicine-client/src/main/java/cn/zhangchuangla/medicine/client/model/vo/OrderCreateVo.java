package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31 
 */
@Data
@Builder
@Schema(description = "创建订单响应对象")
public class OrderCreateVo {

    @Schema(description = "订单号", example = "O2025103011223344")
    private String orderNo;

    @Schema(description = "金额", example = "128.50")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态", example = "WaitPay")
    private String status;

    @Schema(description = "创建时间", example = "2025-10-30 13:22:33")
    private Date createTime;

    @Schema(description = "过期时间", example = "2025-10-30 13:37:33")
    private Date expireTime;

    @Schema(description = "商品摘要", example = "复方感冒灵颗粒  2盒")
    private String productSummary;

}
