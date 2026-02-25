package cn.zhangchuangla.medicine.agent.model.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端用户消费信息。
 */
@Data
@Schema(description = "管理端用户消费信息")
public class UserConsumeInfoVo {

    @Schema(description = "索引")
    private Long index;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "商品总价")
    private BigDecimal totalPrice;

    @Schema(description = "实付金额")
    private BigDecimal payPrice;

    @Schema(description = "完成时间")
    private Date finishTime;
}
