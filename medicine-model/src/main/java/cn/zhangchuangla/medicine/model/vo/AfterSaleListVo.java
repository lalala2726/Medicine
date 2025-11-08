package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 售后列表视图对象
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "售后列表项", example = "售后列表项示例")
public class AfterSaleListVo {

    /**
     * 售后申请ID
     */
    @Schema(description = "售后申请ID", example = "1")
    private Long id;

    /**
     * 售后单号
     */
    @Schema(description = "售后单号", example = "AS20251108001")
    private String afterSaleNo;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "O20251108001")
    private String orderNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "感冒药")
    private String productName;

    /**
     * 商品图片
     */
    @Schema(description = "商品图片", example = "https://example.com/image.jpg")
    private String productImage;

    /**
     * 售后类型
     */
    @Schema(description = "售后类型", example = "REFUND")
    private String afterSaleType;

    /**
     * 售后类型名称
     */
    @Schema(description = "售后类型名称", example = "退款")
    private String afterSaleTypeName;

    /**
     * 售后状态
     */
    @Schema(description = "售后状态", example = "PROCESSING")
    private String afterSaleStatus;

    /**
     * 售后状态名称
     */
    @Schema(description = "售后状态名称", example = "处理中")
    private String afterSaleStatusName;

    /**
     * 退款金额
     */
    @Schema(description = "退款金额", example = "99.99")
    private BigDecimal refundAmount;

    /**
     * 申请原因名称
     */
    @Schema(description = "申请原因名称", example = "质量问题")
    private String applyReasonName;

    /**
     * 申请时间
     */
    @Schema(description = "申请时间", example = "2025-11-08 10:00:00")
    private Date applyTime;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间", example = "2025-11-08 15:30:00")
    private Date auditTime;
}

