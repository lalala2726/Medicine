package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31 21:22
 */
@Data
@Schema(description = "订单列表视图对象")
public class MallOrderListVo {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号（业务唯一标识）
     */
    private String orderNo;

    /**
     * 订单总金额（含运费）
     */
    private BigDecimal totalAmount;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式编码（大驼峰）
     */
    private String payType;

    /**
     * 订单状态编码（大驼峰）
     */
    private String orderStatus;

    /**
     * 配送方式编码（大驼峰）
     */
    private String deliveryType;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 创建时间
     */
    private Date createTime;

}
