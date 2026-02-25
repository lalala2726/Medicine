package cn.zhangchuangla.medicine.client.security;

import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        Set<String> roles = Optional.ofNullable(userService.getUserRolesByUserId(user.getId()))
                .filter(set -> !set.isEmpty())
                .orElseGet(Collections::emptySet);

        AuthUser authUser = BeanCotyUtils.copyProperties(user, AuthUser.class);
        authUser.setPassword(user.getPassword());
        authUser.setRoles(roles);
        return new SysUserDetails(authUser);
    }
}
