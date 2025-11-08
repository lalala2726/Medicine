package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 售后详情视图对象
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "售后详情")
public class AfterSaleDetailVo {

    /**
     * 售后申请ID
     */
    @Schema(description = "售后申请ID",example = "1")
    private Long id;

    /**
     * 售后单号
     */
    @Schema(description = "售后单号", example = "AS20251108001")
    private String afterSaleNo;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1001")
    private Long orderId;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "ORD20251108001")
    private String orderNo;

    /**
     * 订单项ID
     */
    @Schema(description = "订单项ID", example = "2001")
    private Long orderItemId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "3001")
    private Long userId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    /**
     * 售后类型
     */
    @Schema(description = "售后类型", example = "REFUND")
    private String afterSaleType;

    /**
     * 售后类型名称
     */
    @Schema(description = "售后类型名称", example = "仅退款")
    private String afterSaleTypeName;

    /**
     * 售后状态
     */
    @Schema(description = "售后状态", example = "PENDING")
    private String afterSaleStatus;

    /**
     * 售后状态名称
     */
    @Schema(description = "售后状态名称", example = "待审核")
    private String afterSaleStatusName;

    /**
     * 退款金额
     */
    @Schema(description = "退款金额", example = "99.99")
    private BigDecimal refundAmount;

    /**
     * 申请原因
     */
    @Schema(description = "申请原因", example = "PRODUCT_DAMAGE")
    private String applyReason;

    /**
     * 申请原因名称
     */
    @Schema(description = "申请原因名称", example = "商品损坏")
    private String applyReasonName;

    /**
     * 详细说明
     */
    @Schema(description = "详细说明", example = "收到的商品有明显破损")
    private String applyDescription;

    /**
     * 凭证图片列表
     */
    @Schema(description = "凭证图片列表", example = "[\"http://example.com/image1.jpg\",\"http://example.com/image2.jpg\"]")
    private List<String> evidenceImages;

    /**
     * 收货状态
     */
    @Schema(description = "收货状态", example = "RECEIVED")
    private String receiveStatus;

    /**
     * 收货状态名称
     */
    @Schema(description = "收货状态名称", example = "已收货")
    private String receiveStatusName;

    /**
     * 拒绝原因
     */
    @Schema(description = "拒绝原因", example = "证据不足")
    private String rejectReason;

    /**
     * 管理员备注
     */
    @Schema(description = "管理员备注", example = "已核实，符合退款条件")
    private String adminRemark;

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

    /**
     * 完成时间
     */
    @Schema(description = "完成时间", example = "2025-11-09 10:00:00")
    private Date completeTime;

    /**
     * 商品信息
     */
    @Schema(description = "商品信息")
    private ProductInfo productInfo;

    /**
     * 时间线列表
     */
    @Schema(description = "时间线列表")
    private List<AfterSaleTimelineVo> timeline;

    /**
     * 商品信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "商品信息")
    public static class ProductInfo {

        /**
         * 商品ID
         */
        @Schema(description = "商品ID", example = "4001")
        private Long productId;

        /**
         * 商品名称
         */
        @Schema(description = "商品名称", example = "医用口罩")
        private String productName;

        /**
         * 商品图片
         */
        @Schema(description = "商品图片", example = "http://example.com/product.jpg")
        private String productImage;

        /**
         * 商品单价
         */
        @Schema(description = "商品单价", example = "29.99")
        private BigDecimal productPrice;

        /**
         * 购买数量
         */
        @Schema(description = "购买数量", example = "2")
        private Integer quantity;

        /**
         * 小计金额
         */
        @Schema(description = "小计金额", example = "59.98")
        private BigDecimal totalPrice;
    }
}

