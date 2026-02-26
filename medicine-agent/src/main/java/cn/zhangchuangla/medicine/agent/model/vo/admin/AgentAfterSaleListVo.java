package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentFieldDesc;
import cn.zhangchuangla.medicine.agent.annotation.AgentVoDesc;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端智能体售后列表视图。
 */
@Schema(description = "管理端智能体售后列表")
@AgentVoDesc("管理端智能体售后列表")
@Data
public class AgentAfterSaleListVo {

    @Schema(description = "售后申请ID", example = "1")
    @AgentFieldDesc("售后申请ID")
    private Long id;

    @Schema(description = "售后单号", example = "AS20251108001")
    @AgentFieldDesc("售后单号")
    private String afterSaleNo;

    @Schema(description = "订单编号", example = "O20251108001")
    @AgentFieldDesc("订单编号")
    private String orderNo;

    @Schema(description = "用户ID", example = "1001")
    @AgentFieldDesc("用户ID")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    @AgentFieldDesc("用户昵称")
    private String userNickname;

    @Schema(description = "商品名称", example = "感冒药")
    @AgentFieldDesc("商品名称")
    private String productName;

    @Schema(description = "商品图片", example = "https://example.com/image.jpg")
    @AgentFieldDesc("商品图片")
    private String productImage;

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

    @Schema(description = "售后状态名称", example = "处理中")
    @AgentFieldDesc("售后状态名称")
    private String afterSaleStatusName;

    @Schema(description = "退款金额", example = "99.99")
    @AgentFieldDesc("退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "申请原因名称", example = "质量问题")
    @AgentFieldDesc("申请原因名称")
    private String applyReasonName;

    @Schema(description = "申请时间", example = "2025-11-08 10:00:00")
    @AgentFieldDesc("申请时间")
    private Date applyTime;

    @Schema(description = "审核时间", example = "2025-11-08 15:30:00")
    @AgentFieldDesc("审核时间")
    private Date auditTime;
}
