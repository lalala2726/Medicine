package cn.zhangchuangla.medicine.agent.security;

import cn.zhangchuangla.medicine.common.core.constants.Constants;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.AuthContextDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentAuthRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * AI Agent 侧用户查询实现，负责将业务用户转换为通用安全模型。
 */
@Service
public class AgentSecurityUserService implements UserDetailsService {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 3000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentAuthRpcService adminAgentAuthRpcService;

    /**
     * 按用户名加载用户并标准化角色/权限集合。
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        AuthContextDto authContext = adminAgentAuthRpcService.getByUsername(username);
        User user = authContext == null ? null : authContext.getUser();
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        Set<String> roles = SecurityUtils.normalizeRoleCodes(authContext.getRoles());
        Set<String> permissions = SecurityUtils.toPermissionAuthorities(authContext.getPermissions());
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
