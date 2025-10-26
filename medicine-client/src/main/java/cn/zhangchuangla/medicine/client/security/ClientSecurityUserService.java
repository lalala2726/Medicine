package cn.zhangchuangla.medicine.client.security;

import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.common.core.constants.Constants;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 客户端的用户查询实现，负责将业务用户转换为通用的安全模型。
 */
@Service
@RequiredArgsConstructor
public class ClientSecurityUserService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        Set<String> roles = Optional.ofNullable(userService.getUserRolesByUserId(user.getId()))
                .filter(set -> !set.isEmpty())
                .orElseGet(java.util.Collections::emptySet);

        boolean unlocked = Objects.equals(user.getStatus(), Constants.ACCOUNT_UNLOCK_KEY);

        AuthUser authUser = AuthUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .status(user.getStatus())
                .roles(roles)
                .enabled(unlocked)
                .accountNonLocked(unlocked)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();
        return new SysUserDetails(authUser);
    }
}
