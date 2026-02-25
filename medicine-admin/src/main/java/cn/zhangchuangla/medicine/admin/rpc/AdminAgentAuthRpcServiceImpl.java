package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.service.PermissionService;
import cn.zhangchuangla.medicine.admin.service.RoleService;
import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.dubbo.api.admin.AdminAgentAuthRpcService;
import cn.zhangchuangla.medicine.model.dto.AuthContextDto;
import cn.zhangchuangla.medicine.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 管理端 Agent 认证上下文 RPC Provider。
 */
@DubboService(interfaceClass = AdminAgentAuthRpcService.class, group = "medicine-admin", version = "1.0.0")
@RequiredArgsConstructor
public class AdminAgentAuthRpcServiceImpl implements AdminAgentAuthRpcService {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;

    @Override
    public AuthContextDto getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            return null;
        }
        return buildContext(user);
    }

    @Override
    public AuthContextDto getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        User user = userService.getUserByUsername(username.trim());
        if (user == null) {
            return null;
        }
        return buildContext(user);
    }

    private AuthContextDto buildContext(User user) {
        Set<String> roleCodes = SecurityUtils.normalizeRoleCodes(roleService.getUserRoleByUserId(user.getId()));
        Set<String> permissionCodes = SecurityUtils.toPermissionAuthorities(permissionService.getPermissionCodesByUserId(user.getId()));

        AuthContextDto dto = new AuthContextDto();
        dto.setUser(user);
        dto.setRoles(roleCodes);
        dto.setPermissions(permissionCodes);
        return dto;
    }
}
