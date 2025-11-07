package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/7 14:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreezeOrUnUserWalletRequest {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 关闭原因
     */
    @Schema(description = "关闭原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户取消")
    @NotBlank(message = "关闭原因不能为空")
    private String reason;

}
