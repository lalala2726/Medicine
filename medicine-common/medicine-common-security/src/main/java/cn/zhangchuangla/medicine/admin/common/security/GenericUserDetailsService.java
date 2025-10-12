package cn.zhangchuangla.medicine.admin.common.security;

import cn.zhangchuangla.medicine.admin.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.admin.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.admin.common.security.spi.SecurityUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 通用 UserDetailsService，通过 SPI 获取不同端的用户信息。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenericUserDetailsService implements UserDetailsService {

    private final SecurityUserService securityUserService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        AuthUser authUser = securityUserService.loadUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        log.debug("加载用户信息: {}", username);
        return new SysUserDetails(authUser);
    }
}
