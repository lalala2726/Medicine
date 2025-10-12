package cn.zhangchuangla.medicine.security.spi;

import cn.zhangchuangla.medicine.security.entity.AuthUser;

import java.util.Optional;

/**
 * 对外暴露的用户查询 SPI，由具体业务模块实现。
 */
public interface SecurityUserService {

    /**
     * 按登录名加载用户。
     *
     * @param username 登录名
     * @return 用户信息
     */
    Optional<AuthUser> loadUserByUsername(String username);
}
