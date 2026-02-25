package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.dubbo.api.admin.AdminAgentAuthRpcService;
import cn.zhangchuangla.medicine.dubbo.api.client.ClientAgentUserRpcService;
import cn.zhangchuangla.medicine.model.dto.AuthContextDto;
import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Agent 用户服务 Dubbo Consumer 实现。
 */
@Service
public class UserServiceImpl implements UserService {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 3000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentAuthRpcService adminAgentAuthRpcService;

    @DubboReference(group = "medicine-client", version = "1.0.0", check = false, timeout = 3000, retries = 0,
            url = "${dubbo.references.medicine-client.url:}")
    private ClientAgentUserRpcService clientAgentUserRpcService;

    @Override
    public UserVo getCurrentUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return clientAgentUserRpcService.getCurrentUser(userId);
    }

    @Override
    public AuthUserDto getUser(Long userId) {
        AuthContextDto context = getAuthContextByUserId(userId);
        if (context == null || context.getUser() == null) {
            return null;
        }
        return BeanCotyUtils.copyProperties(context.getUser(), AuthUserDto.class);
    }

    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        AuthContextDto context = adminAgentAuthRpcService.getByUsername(username.trim());
        return context == null ? null : context.getUser();
    }

    @Override
    public Set<String> getUserRolesByUserId(Long userId) {
        AuthContextDto context = getAuthContextByUserId(userId);
        if (context == null) {
            return Set.of();
        }
        return SecurityUtils.normalizeCodes(context.getRoles());
    }

    @Override
    public Set<String> getUserPermissionCodesByUserId(Long userId) {
        AuthContextDto context = getAuthContextByUserId(userId);
        if (context == null) {
            return Set.of();
        }
        return SecurityUtils.normalizeCodes(context.getPermissions());
    }

    private AuthContextDto getAuthContextByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return adminAgentAuthRpcService.getByUserId(userId);
    }
}
