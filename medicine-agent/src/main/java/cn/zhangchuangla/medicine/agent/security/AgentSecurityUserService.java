package cn.zhangchuangla.medicine.agent.security;

import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.constants.Constants;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI Agent 侧用户查询实现，负责将业务用户转换为通用安全模型。
 */
@Service
@RequiredArgsConstructor
public class AgentSecurityUserService implements UserDetailsService {

    private final UserService userService;

    /**
     * 按用户名加载用户并标准化角色/权限集合。
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        Set<String> roles = SecurityUtils.normalizeRoleCodes(Optional.ofNullable(userService.getUserRolesByUserId(user.getId()))
                .filter(set -> !set.isEmpty())
                .orElseGet(Collections::emptySet));
        Set<String> permissions = SecurityUtils.toPermissionAuthorities(
                Optional.ofNullable(userService.getUserPermissionCodesByUserId(user.getId()))
                        .filter(set -> !set.isEmpty())
                        .orElseGet(Collections::emptySet));
        boolean unlocked = Objects.equals(user.getStatus(), Constants.ACCOUNT_UNLOCK_KEY);

        AuthUser authUser = AuthUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .status(user.getStatus())
                .roles(roles)
                .permissions(permissions)
                .enabled(unlocked)
                .accountNonLocked(unlocked)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        SecurityUtils.toRoleAuthorities(roles)
                .forEach(roleAuthority -> authorities.add(new SimpleGrantedAuthority(roleAuthority)));
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(authorities);
        return userDetails;
    }
}
