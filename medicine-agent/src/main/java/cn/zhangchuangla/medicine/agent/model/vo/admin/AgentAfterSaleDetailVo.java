package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentFieldDesc;
import cn.zhangchuangla.medicine.agent.annotation.AgentVoDesc;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 管理端智能体售后详情视图。
 */
@Schema(description = "管理端智能体售后详情")
@AgentVoDesc("管理端智能体售后详情")
@Data
public class AgentAfterSaleDetailVo {

    @Schema(description = "售后申请ID", example = "1")
    @AgentFieldDesc("售后申请ID")
    private Long id;

    @Schema(description = "售后单号", example = "AS20251108001")
    @AgentFieldDesc("售后单号")
    private String afterSaleNo;

    @Schema(description = "订单ID", example = "1001")
    @AgentFieldDesc("订单ID")
    private Long orderId;

    @Schema(description = "订单编号", example = "ORD20251108001")
    @AgentFieldDesc("订单编号")
    private String orderNo;

    @Schema(description = "订单项ID", example = "2001")
    @AgentFieldDesc("订单项ID")
    private Long orderItemId;

    @Schema(description = "用户ID", example = "3001")
    @AgentFieldDesc("用户ID")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    @AgentFieldDesc("用户昵称")
    private String userNickname;

    @Schema(description = "售后类型")
    @AgentFieldDesc("售后类型")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_AFTER_SALE_TYPE)
    private String afterSaleType;

    @Schema(description = "售后类型名称", example = "仅退款")
    @AgentFieldDesc("售后类型名称")
    private String afterSaleTypeName;

    @Schema(description = "售后状态")
    @AgentFieldDesc("售后状态")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_AFTER_SALE_STATUS)
    private String afterSaleStatus;

    @Schema(description = "售后状态名称", example = "待审核")
    @AgentFieldDesc("售后状态名称")
    private String afterSaleStatusName;

    @Schema(description = "退款金额", example = "99.99")
    @AgentFieldDesc("退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "申请原因")
    @AgentFieldDesc("申请原因")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_AFTER_SALE_REASON)
    private String applyReason;

    @Schema(description = "申请原因名称", example = "商品损坏")
    @AgentFieldDesc("申请原因名称")
    private String applyReasonName;

    @Schema(description = "详细说明", example = "收到的商品有明显破损")
    @AgentFieldDesc("详细说明")
    private String applyDescription;

    @Schema(description = "凭证图片列表", example = "[\"http://example.com/image1.jpg\",\"http://example.com/image2.jpg\"]")
    @AgentFieldDesc("凭证图片列表")
    private List<String> evidenceImages;

    @Schema(description = "收货状态")
    @AgentFieldDesc("收货状态")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_AFTER_SALE_RECEIVE_STATUS)
    private String receiveStatus;

    @Schema(description = "收货状态名称", example = "已收货")
    @AgentFieldDesc("收货状态名称")
    private String receiveStatusName;

    @Schema(description = "拒绝原因", example = "证据不足")
    @AgentFieldDesc("拒绝原因")
    private String rejectReason;

    @Schema(description = "管理员备注", example = "已核实，符合退款条件")
    @AgentFieldDesc("管理员备注")
    private String adminRemark;

    @Schema(description = "申请时间", example = "2025-11-08 10:00:00")
    @AgentFieldDesc("申请时间")
    private Date applyTime;

    @Schema(description = "审核时间", example = "2025-11-08 15:30:00")
    @AgentFieldDesc("审核时间")
    private Date auditTime;

    @Schema(description = "完成时间", example = "2025-11-09 10:00:00")
    @AgentFieldDesc("完成时间")
    private Date completeTime;

    @Schema(description = "商品信息")
    @AgentFieldDesc("商品信息")
    private ProductInfo productInfo;

    @Schema(description = "时间线列表")
    @AgentFieldDesc("时间线列表")
    private List<AgentAfterSaleTimelineVo> timeline;

    @Data
    @Schema(description = "商品信息")
    @AgentVoDesc("商品信息")
    public static class ProductInfo {

        @Schema(description = "商品ID", example = "4001")
        @AgentFieldDesc("商品ID")
        private Long productId;

        @Schema(description = "商品名称", example = "医用口罩")
        @AgentFieldDesc("商品名称")
        private String productName;

        @Schema(description = "商品图片", example = "http://example.com/product.jpg")
        @AgentFieldDesc("商品图片")
        private String productImage;

        @Schema(description = "商品单价", example = "29.99")
        @AgentFieldDesc("商品单价")
        private BigDecimal productPrice;

        @Schema(description = "购买数量", example = "2")
        @AgentFieldDesc("购买数量")
        private Integer quantity;

        @Schema(description = "小计金额", example = "59.98")
        @AgentFieldDesc("小计金额")
        private BigDecimal totalPrice;
    }
}
