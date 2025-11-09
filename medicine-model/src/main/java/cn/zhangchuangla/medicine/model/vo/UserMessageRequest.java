package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/12
 */
@Data
public class UserMessageRequest {

    @Schema(description = "用户输入")
    @NotEmpty(message = "用户输入不能为空")
    private String message;

    @Schema(description = "会话ID")
    private String uuid;
}
