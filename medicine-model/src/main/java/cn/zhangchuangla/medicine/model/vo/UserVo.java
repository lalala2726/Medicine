package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户
 */
@Schema(description = "用户信息视图对象")
@Data
public class UserVo {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickName;

    /**
     * 头像
     */
    @Schema(description = "头像", example = "https://zhangchuangla.cn/avatar.png")
    private String avatar;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800000000")
    private String phoneNumber;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "男")
    private String gender;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "正常")
    private String status;
}
