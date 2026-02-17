package cn.zhangchuangla.medicine.admin.security;

import cn.zhangchuangla.medicine.admin.service.PermissionService;
import cn.zhangchuangla.medicine.admin.service.RoleService;
import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.common.core.constants.Constants;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 管理端的用户查询实现，负责将业务用户转换为通用˚的安全模型。
 */
@Service
@RequiredArgsConstructor
public class AdminSecurityUserService implements UserDetailsService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        Set<String> roles = normalizeRoleCodes(Optional.ofNullable(roleService.getUserRoleByUserId(user.getId()))
                .filter(set -> !set.isEmpty())
                .orElseGet(Collections::emptySet));
        Set<String> permissions = normalizePermissionCodes(
                Optional.ofNullable(permissionService.getPermissionCodesByUserId(user.getId()))
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
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

    private Set<String> normalizeRoleCodes(Set<String> roleCodes) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            if (roleCode == null) {
                continue;
            }
            String trimmed = roleCode.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.regionMatches(true, 0, ROLE_PREFIX, 0, ROLE_PREFIX.length())) {
                trimmed = trimmed.substring(ROLE_PREFIX.length()).trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
            }
            normalized.add(trimmed);
        }
        if (normalized.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(normalized);
    }

    private Set<String> normalizePermissionCodes(Set<String> permissionCodes) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String permissionCode : permissionCodes) {
            if (permissionCode == null) {
                continue;
            }
            String trimmed = permissionCode.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        if (normalized.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(normalized);
    }
}
