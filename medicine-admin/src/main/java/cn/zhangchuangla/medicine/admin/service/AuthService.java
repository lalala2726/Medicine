package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.model.vo.CurrentUserInfoVo;

import java.util.Set;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28
 */
public interface AuthService {

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

    /**
     * 获取当前登录用户信息
     */
    CurrentUserInfoVo currentUserInfo();

    /**
     * 获取当前登录用户权限
     *
     * @return 权限列表
     */
    Set<String> currentUserPermissions();
}
