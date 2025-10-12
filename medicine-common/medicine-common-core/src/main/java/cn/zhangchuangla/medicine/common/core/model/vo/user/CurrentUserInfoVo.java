package cn.zhangchuangla.medicine.common.core.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息视图对象
 */
@Data
@Schema(name = "CurrentUserInfoVo", description = "当前登录用户信息")
public class CurrentUserInfoVo {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色标识")
    private String roles;
}
