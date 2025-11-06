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
 * created on 2025/11/7 04:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户钱包流水信息")
public class UserWalletFlowInfoVo {

    /**
     * 流水索引
     */
    @Schema(description = "流水索引", example = "1")
    private Long index;


    /**
     * 变动类型
     */
    @Schema(description = "变动类型", example = "充值")
    private String changeType;

    /**
     * 变动金额
     */
    @Schema(description = "变动金额", example = "10.00")
    private BigDecimal amount;

    /**
     * 变动前余额
     */
    @Schema(description = "变动前余额", example = "10.00")
    private BigDecimal beforeBalance;

    /**
     * 变动后余额
     */
    @Schema(description = "变动后余额", example = "10.00")
    private BigDecimal afterBalance;

    /**
     * 变动时间
     */
    @Schema(description = "变动时间", example = "2025-11-07 04:33:00")
    private Date changeTime;

}
