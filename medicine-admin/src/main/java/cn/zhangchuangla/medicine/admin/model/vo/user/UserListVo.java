package cn.zhangchuangla.medicine.admin.model.vo.user;

import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 用户
 */
@Schema(description = "用户列表视图对象")
@Data
public class UserListVo {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", type = "int", format = "int64", example = "1")
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
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", type = "date", example = "2023-01-01 00:00:00")
    private Date createTime;

}
