package cn.zhangchuangla.medicine.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退出登录响应对象
 *
 * @author Chuang
 * @since 2025/9/18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponse {

    /**
     * 退出是否成功
     */
    private Boolean success;

    /**
     * 跳转URL
     */
    private String redirectUrl;

    /**
     * 消息
     */
    private String message;

    /**
     * 构建成功的退出响应
     */
    public static LogoutResponse success(String message, String redirectUrl) {
        return LogoutResponse.builder()
                .success(true)
                .message(message)
                .redirectUrl(redirectUrl)
                .build();
    }

    /**
     * 构建失败的退出响应
     */
    public static LogoutResponse failure(String message) {
        return LogoutResponse.builder()
                .success(false)
                .message(message)
                .redirectUrl("/login")
                .build();
    }
}