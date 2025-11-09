package cn.zhangchuangla.medicine.model.request.user;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户列表查询参数")
@Data
public class UserListQueryRequest extends PageRequest {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    /**
     * 角色
     */
    @Schema(description = "角色", example = "admin")
    private String roles;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;


    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "admin")
    private String createBy;

}
