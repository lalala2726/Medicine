package cn.zhangchuangla.medicine.dubbo.api.model;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理端智能体订单分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminOrderListQuery extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;

    private String payType;

    private String orderStatus;

    private String deliveryType;

    private String receiverName;

    private String receiverPhone;
}
