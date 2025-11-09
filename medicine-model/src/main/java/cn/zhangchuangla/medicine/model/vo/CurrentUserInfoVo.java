package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息视图对象
 */
@Data
@Schema(name = "CurrentUserInfoVo", description = "当前登录用户信息")
public class CurrentUserInfoVo {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "角色标识", example = "ROLE_USER")
    private String roles;
}
