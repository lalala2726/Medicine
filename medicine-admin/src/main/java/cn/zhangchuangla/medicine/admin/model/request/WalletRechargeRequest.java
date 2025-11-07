package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/7 14:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "钱包充值请求参数")
public class WalletRechargeRequest {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 金额
     */
    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.00")
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    /**
     * 充值原因
     */
    @Schema(description = "充值原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "充值")
    @NotBlank(message = "充值原因不能为空")
    private String reason;


}
