package cn.zhangchuangla.medicine.agent.security;

import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.constants.Constants;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AgentSecurityUserServiceTests {

    @Test
    void loadUserByUsername_ShouldCombineRolesAndPermissionsAuthorities() {
        StubUserService userService = new StubUserService();
        User user = new User();
        user.setId(1001L);
        user.setUsername("admin");
        user.setPassword("pwd");
        user.setStatus(Constants.ACCOUNT_UNLOCK_KEY);
        userService.userByUsername = user;
        userService.roles = Set.of("admin", "super_admin");
        userService.permissions = Set.of("mall:product:list", "system:user:query");

        AgentSecurityUserService securityUserService = new AgentSecurityUserService(userService);

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
    void loadUserByUsername_ShouldNormalizeDirtyRoleAndPermissionCodes() {
        StubUserService userService = new StubUserService();
        User user = new User();
        user.setId(1002L);
        user.setUsername("cleaner");
        user.setPassword("pwd");
        user.setStatus(Constants.ACCOUNT_UNLOCK_KEY);
        userService.userByUsername = user;
        userService.roles = new LinkedHashSet<>(Arrays.asList(" ROLE_admin ", "admin", " ", "ROLE_super_admin", null));
        userService.permissions = new LinkedHashSet<>(Arrays.asList(
                " mall:product:list ", "mall:product:list", " ", "system:user:query", null));

        AgentSecurityUserService securityUserService = new AgentSecurityUserService(userService);

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
    void loadUserByUsername_ShouldThrow_WhenUserNotExists() {
        StubUserService userService = new StubUserService();
        AgentSecurityUserService securityUserService = new AgentSecurityUserService(userService);

        assertThrows(UsernameNotFoundException.class, () -> securityUserService.loadUserByUsername("missing"));
    }

    private static class StubUserService implements UserService {

        private User userByUsername;
        private Set<String> roles = Set.of();
        private Set<String> permissions = Set.of();

        @Override
        public UserVo getCurrentUser(Long userId) {
            return null;
        }

        @Override
        public AuthUserDto getUser(Long userId) {
            return null;
        }

        @Override
        public User getUserByUsername(String username) {
            return userByUsername;
        }

        @Override
        public Set<String> getUserRolesByUserId(Long userId) {
            return roles;
        }

        @Override
        public Set<String> getUserPermissionCodesByUserId(Long userId) {
            return permissions;
        }
    }
}
