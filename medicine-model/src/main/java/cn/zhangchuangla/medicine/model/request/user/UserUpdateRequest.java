package cn.zhangchuangla.medicine.model.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户
 */
@Schema(description = "用户修改请求对象")
@Data
public class UserUpdateRequest {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", type = "long", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", type = "string", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", type = "string", example = "张三")
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像URL", type = "string", example = "https://example.com/avatar.jpg")
    private String avatar;

    /**
     * 密码
     */
    @Schema(description = "密码", type = "string", example = "123456")
    private String password;

    /**
     * 角色
     */
    @Schema(description = "角色", type = "string", example = "admin")
    private String roles;

    /**
     * 状态
     */
    @Schema(description = "状态", type = "int", example = "1")
    private Integer status;

    /**
     * 身份证号码
     */
    @Schema(description = "身份证号码", type = "string", example = "123456789012345678")
    private String idCard;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", type = "string", example = "13800000000")
    private String phoneNumber;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", type = "string", example = "张三")
    private String realName;

}
