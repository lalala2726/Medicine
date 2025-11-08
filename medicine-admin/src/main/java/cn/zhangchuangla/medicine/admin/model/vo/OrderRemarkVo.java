package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单备注信息VO
 *
 * @author Chuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单备注信息")
public class OrderRemarkVo {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    /**
     * 订单号
     */
    @Schema(description = "订单号", example = "ORDER202307190001")
    private String orderNo;

    /**
     * 订单备注
     */
    @Schema(description = "订单备注", example = "请尽快发货")
    private String remark;

    /**
     * 用户留言
     */
    @Schema(description = "用户留言", example = "希望包装严实一点")
    private String note;
}

