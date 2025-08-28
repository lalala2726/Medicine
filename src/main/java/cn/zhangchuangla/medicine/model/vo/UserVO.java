package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息视图对象
 */
@Data
@Schema(name = "UserVO", description = "当前登录用户信息")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色标识")
    private String roles;

    @Schema(description = "账户状态")
    private Integer status;
}
