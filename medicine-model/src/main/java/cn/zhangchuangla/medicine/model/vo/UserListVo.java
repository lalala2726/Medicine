package cn.zhangchuangla.medicine.model.vo;

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

    @Schema(description = "用户ID", format = "int64", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "角色", example = "admin")
    private String roles;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2023-01-01 00:00:00")
    private Date createTime;

}
