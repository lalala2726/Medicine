package cn.zhangchuangla.medicine.model.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28 16:27
 */
@Schema(description = "刷新令牌请求参数")
@Data
public class RefreshRequest {

    @Schema(description = "刷新令牌", type = "string", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5MjE5M")
    private String refreshToken;
}
