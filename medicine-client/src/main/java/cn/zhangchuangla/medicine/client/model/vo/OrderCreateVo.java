package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31 01:54
 */
@Data
@Builder
@Schema(description = "创建订单响应对象")
public class OrderCreateVo {

    /**
     * 订单号
     */
    @Schema(description = "订单号", type = "string", example = "O2025103011223344")
    private String orderNo;

    /**
     * 商户订单号
     */
    @Schema(description = "商户订单号", type = "string", example = "O2025103011223344")
    private String outTradeNo;

    /**
     * 金额
     */
    @Schema(description = "金额", type = "number", example = "128.50")
    private BigDecimal totalAmount;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式", type = "string", example = "alipay")
    private String payType;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态", type = "string", example = "WAIT_PAY")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "string", example = "2025-10-30 13:22:33")
    private Date createTime;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间", type = "string", example = "2025-10-30 13:37:33")
    private Date expireTime;

    /**
     * 商品摘要
     */
    @Schema(description = "商品摘要", type = "string", example = "复方感冒灵颗粒  2盒")
    private String productSummary;

    /**
     * 跳转地址
     */
    @Schema(description = "跳转地址", type = "string", example = "/pay/confirm?orderNo=O2025103011223344")
    private String redirectUrl;

}
