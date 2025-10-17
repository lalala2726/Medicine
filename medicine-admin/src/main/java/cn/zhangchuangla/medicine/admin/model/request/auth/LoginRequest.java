package cn.zhangchuangla.medicine.admin.model.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28 16:27
 */
@Schema(description = "登录请求参数")
@Data
public class LoginRequest {

    @Schema(description = "用户名", type = "string", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String username;

    @Schema(description = "密码", type = "string", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;
}
