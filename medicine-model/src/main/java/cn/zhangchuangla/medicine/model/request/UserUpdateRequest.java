package cn.zhangchuangla.medicine.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户
 */
@Schema(description = "用户修改请求对象")
@Data
public class UserUpdateRequest {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "角色", example = "admin")
    private String roles;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "身份证号码", example = "123456789012345678")
    private String idCard;

    @Schema(description = "手机号码", example = "13800000000")
    private String phoneNumber;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

}
