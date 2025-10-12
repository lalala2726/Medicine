package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28 16:18
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 新用户ID
     */
    Long register(String username, String password);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 授权令牌
     */
    AuthTokenVo login(String username, String password);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌JWT
     * @return 新的访问令牌与原刷新令牌
     */
    AuthTokenVo refresh(String refreshToken);

    /**
     * 退出登录
     *
     * @param accessToken 访问令牌
     */
    void logout(String accessToken);
}
