package cn.zhangchuangla.medicine.model.request.graphql;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * GraphQL 订单查询对象
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "GraphQL 订单查询对象")
@Data
public class GraphQLOrderQuery extends PageRequest {

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "ORD202602120001")
    private String orderNo;

    /**
     * 支付方式编码
     */
    @Schema(description = "支付方式编码", example = "ALIPAY")
    private String payType;

    /**
     * 订单状态编码
     */
    @Schema(description = "订单状态编码", example = "PENDING_PAYMENT")
    private String orderStatus;

    /**
     * 配送方式编码
     */
    @Schema(description = "配送方式编码", example = "EXPRESS")
    private String deliveryType;

    /**
     * 收货人姓名
     */
    @Schema(description = "收货人姓名", example = "张三")
    private String receiverName;

    /**
     * 收货人手机号
     */
    @Schema(description = "收货人手机号", example = "13800000000")
    private String receiverPhone;
}
