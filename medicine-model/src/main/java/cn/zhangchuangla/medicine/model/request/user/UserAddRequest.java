package cn.zhangchuangla.medicine.model.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

/**
 * 用户
 */
@Schema(description = "用户添加请求对象")
@Data
public class UserAddRequest {

    /**
     * 用户名
     */
    @Schema(description = "用户名", type = "string", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
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
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 角色
     */
    @Schema(description = "角色", type = "string", example = "admin")
    private Set<String> roles;

    /**
     * 状态
     */
    @Schema(description = "状态", type = "int", example = "1")
    private Integer status;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", type = "string", example = "zhangsan@example.com")
    private String email;

    /**
     * 身份证号码
     */
    @Schema(description = "身份证号码", type = "string", example = "123456789012345678")
    private String idCard;

    /**
     * 手机号
     */
    @Schema(description = "手机号", type = "string", example = "13800000000")
    private String phoneNumber;

    /**
     * 性别
     */
    @Schema(description = "性别", type = "int", example = "1")
    private Integer gender;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", type = "string", example = "张三")
    private String realName;

}
