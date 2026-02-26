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
 * 管理端用户钱包信息。
 */
@Data
@Schema(description = "管理端用户钱包信息")
@AgentVoDesc("管理端用户钱包信息")
public class UserWalletVo {

    @Schema(description = "用户ID")
    @AgentFieldDesc("用户ID")
    private Long userId;

    @Schema(description = "钱包编号")
    @AgentFieldDesc("钱包编号")
    private String walletNo;

    @Schema(description = "可用余额")
    @AgentFieldDesc("可用余额")
    private BigDecimal balance;

    @Schema(description = "累计入账金额")
    @AgentFieldDesc("累计入账金额")
    private BigDecimal totalIncome;

    @Schema(description = "累计支出金额")
    @AgentFieldDesc("累计支出金额")
    private BigDecimal totalExpend;

    @Schema(description = "币种")
    @AgentFieldDesc("币种")
    private String currency;

    @Schema(description = "状态")
    @AgentFieldDesc("状态")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_USER_WALLET_STATUS)
    private Integer status;

    @Schema(description = "冻结原因")
    @AgentFieldDesc("冻结原因")
    private String freezeReason;

    @Schema(description = "冻结时间")
    @AgentFieldDesc("冻结时间")
    private Date freezeTime;

    @Schema(description = "更新时间")
    @AgentFieldDesc("更新时间")
    private Date updatedAt;
}
