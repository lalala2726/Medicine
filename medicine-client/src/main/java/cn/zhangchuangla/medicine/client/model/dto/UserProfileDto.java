package cn.zhangchuangla.medicine.client.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/20
 */
@Data
@Schema(description = "用户信息")
public class UserProfileDto {

    @Schema(description = "头像", example = "https://avatar.csdn.net/6/E/E/3_zhangchuangla.jpg")
    private String avatar;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "手机号", example = "13888888888")
    private String phoneNumber;

    @Schema(description = "生日", example = "2025-11-20")
    private LocalDate birthday;
}
