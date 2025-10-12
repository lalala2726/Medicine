package cn.zhangchuangla.medicine.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28 16:26
 */
@Schema
@Data
public class RegisterRequest {

    @Schema(description = "用户名", type = "string", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String username;

    @Schema(description = "密码", type = "string", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;
}
