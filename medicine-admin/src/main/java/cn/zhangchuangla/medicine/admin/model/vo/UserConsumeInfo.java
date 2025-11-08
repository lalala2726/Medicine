package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/7 07:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户消费信息", example = "UserConsumeInfoExample")
public class UserConsumeInfo {

    /**
     * 索引
     */
    @Schema(description = "索引", example = "1")
    private Long index;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "ORD20251107001")
    private String orderNo;

    /**
     * 商品总价
     */
    @Schema(description = "商品总价", example = "99.99")
    private BigDecimal totalPrice;

    /**
     * 实付金额
     */
    @Schema(description = "实付金额", example = "89.99")
    private BigDecimal payPrice;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间", example = "2025-11-07 15:30:00")
    private Date finishTime;


}
