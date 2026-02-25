package cn.zhangchuangla.medicine.agent.security;

import cn.zhangchuangla.medicine.common.core.constants.Constants;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.dubbo.api.admin.AdminAgentAuthRpcService;
import cn.zhangchuangla.medicine.dubbo.api.model.AdminAuthContextDto;
import cn.zhangchuangla.medicine.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AgentSecurityUserServiceTests {

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void loadUserByUsername_ShouldCombineRolesAndPermissionsAuthorities() throws Exception {
        StubAuthRpcService authRpcService = new StubAuthRpcService();
        authRpcService.context = buildContext(
                1001L,
                "admin",
                "pwd",
                Set.of("admin", "super_admin"),
                Set.of("mall:product:list", "system:user:query")
        );

        AgentSecurityUserService securityUserService = new AgentSecurityUserService();
        setField(securityUserService, "adminAgentAuthRpcService", authRpcService);

        var details = securityUserService.loadUserByUsername("admin");
        assertInstanceOf(SysUserDetails.class, details);
        var sysUserDetails = (SysUserDetails) details;

        Set<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertTrue(authorities.contains("ROLE_admin"));
        assertTrue(authorities.contains("ROLE_super_admin"));
        assertTrue(authorities.contains("mall:product:list"));
        assertTrue(authorities.contains("system:user:query"));
        assertEquals(Set.of("admin", "super_admin"), sysUserDetails.getUser().getRoles());
        assertEquals(Set.of("mall:product:list", "system:user:query"), sysUserDetails.getUser().getPermissions());
    }

    @Test
    void loadUserByUsername_ShouldNormalizeDirtyRoleAndPermissionCodes() throws Exception {
        StubAuthRpcService authRpcService = new StubAuthRpcService();
        authRpcService.context = buildContext(
                1002L,
                "cleaner",
                "pwd",
                new LinkedHashSet<>(Arrays.asList(" ROLE_admin ", "admin", " ", "ROLE_super_admin", null)),
                new LinkedHashSet<>(Arrays.asList(" mall:product:list ", "mall:product:list", " ", "system:user:query", null))
        );

        AgentSecurityUserService securityUserService = new AgentSecurityUserService();
        setField(securityUserService, "adminAgentAuthRpcService", authRpcService);

        var details = (SysUserDetails) securityUserService.loadUserByUsername("cleaner");

        Set<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertEquals(Set.of("ROLE_admin", "ROLE_super_admin", "mall:product:list", "system:user:query"), authorities);
        assertFalse(authorities.contains("ROLE_ROLE_admin"));
        assertEquals(Set.of("admin", "super_admin"), details.getUser().getRoles());
        assertEquals(Set.of("mall:product:list", "system:user:query"), details.getUser().getPermissions());
    }

    @Test
    void loadUserByUsername_ShouldThrow_WhenUserNotExists() throws Exception {
        StubAuthRpcService authRpcService = new StubAuthRpcService();
        authRpcService.context = null;

        AgentSecurityUserService securityUserService = new AgentSecurityUserService();
        setField(securityUserService, "adminAgentAuthRpcService", authRpcService);

        assertThrows(UsernameNotFoundException.class, () -> securityUserService.loadUserByUsername("missing"));
    }

    private AdminAuthContextDto buildContext(Long userId,
                                             String username,
                                             String password,
                                             Set<String> roles,
                                             Set<String> permissions) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword(password);
        user.setStatus(Constants.ACCOUNT_UNLOCK_KEY);

        AdminAuthContextDto context = new AdminAuthContextDto();
        context.setUser(user);
        context.setRoles(roles);
        context.setPermissions(permissions);
        return context;
    }

    private static class StubAuthRpcService implements AdminAgentAuthRpcService {

        private AdminAuthContextDto context;

        @Override
        public AdminAuthContextDto getByUserId(Long userId) {
            return context;
        }

        @Override
        public AdminAuthContextDto getByUsername(String username) {
            return context;
        }
    }
}
