package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户钱包视图对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户钱包视图对象")
public class UserWalletVo {

    /**
     * 用户ID，对应系统用户表ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 钱包编号，唯一标识一个用户的钱包
     */
    @Schema(description = "钱包编号", example = "W2022010100000000000000000001")
    private String walletNo;

    /**
     * 可用余额
     */
    @Schema(description = "可用余额", example = "100.00")
    private BigDecimal balance;

    /**
     * 累计入账金额（充值、退款等）
     */
    @Schema(description = "累计入账金额", example = "100.00")
    private BigDecimal totalIncome;

    /**
     * 累计支出金额（消费、提现等）
     */
    @Schema(description = "累计支出金额", example = "100.00")
    private BigDecimal totalExpend;

    /**
     * 币种，默认人民币
     */
    @Schema(description = "币种", example = "CNY")
    private String currency;

    /**
     * 状态：0正常，1冻结
     */
    @Schema(description = "状态", example = "0")
    private Integer status;

    /**
     * 冻结原因
     */
    @Schema(description = "冻结原因", example = "冻结原因")
    private String freezeReason;

    /**
     * 冻结时间
     */
    @Schema(description = "冻结时间", example = "2022-01-01 00:00:00")
    private Date freezeTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2022-01-01 00:00:00")
    private Date updatedAt;
}
