package cn.zhangchuangla.medicine.common.security.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

class SecurityUtilsTests {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 验证角色提取只返回角色码，不会把权限码误识别为角色。
     */
    @Test
    void getRoles_ShouldOnlyReturnRoleAuthorities() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "tester",
                null,
                createAuthorityList("ROLE_admin", "system:user:list", "super_admin")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var roles = SecurityUtils.getRoles();

        assertTrue(roles.contains("admin"));
        assertTrue(roles.contains("super_admin"));
        assertFalse(roles.contains("system:user:list"));
    }

    /**
     * 验证超级管理员角色在 ROLE_ 前缀场景下可被正确识别。
     */
    @Test
    void isSuperAdmin_ShouldRecognizeSuperAdminRole() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "tester",
                null,
                createAuthorityList("ROLE_super_admin")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(SecurityUtils.isSuperAdmin());
    }

    /**
     * 验证 hasRole 仅支持归一化后的角色码匹配。
     */
    @Test
    void hasRole_ShouldSupportNormalizedRole() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "tester",
                null,
                createAuthorityList("ROLE_admin")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(SecurityUtils.hasRole("admin"));
        assertFalse(SecurityUtils.hasRole("ROLE_admin"));
    }

    /**
     * 验证 hasAnyPermission 只按权限编码精确匹配。
     */
    @Test
    void hasAnyPermission_ShouldMatchExactPermission() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "tester",
                null,
                createAuthorityList("ROLE_admin", "system:user:list")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(SecurityUtils.hasAnyPermission(Set.of("system:user:list")));
        assertFalse(SecurityUtils.hasAnyPermission(Set.of("system:user:query")));
        assertFalse(SecurityUtils.hasAnyPermission(Set.of("ROLE_admin")));
    }

    /**
     * 验证 hasAnyRole 支持 ROLE_ 前缀与纯角色码输入。
     */
    @Test
    void hasAnyRole_ShouldSupportRolePrefixInput() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "tester",
                null,
                createAuthorityList("ROLE_admin")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(SecurityUtils.hasAnyRole(Set.of("ROLE_admin")));
        assertTrue(SecurityUtils.hasAnyRole(Set.of("admin")));
        assertFalse(SecurityUtils.hasAnyRole(Set.of("super_admin")));
    }

    /**
     * 验证未认证时 hasAnyPermission/hasAnyRole 均返回 false。
     */
    @Test
    void hasAnyPermissionAndRole_ShouldReturnFalseWhenUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertFalse(SecurityUtils.hasAnyPermission(Set.of("system:user:list")));
        assertFalse(SecurityUtils.hasAnyRole(Set.of("admin")));
    }

    /**
     * 验证空输入不会命中。
     */
    @Test
    void hasAnyPermissionAndRole_ShouldReturnFalseWhenInputEmpty() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "tester",
                null,
                createAuthorityList("ROLE_admin", "system:user:list")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertFalse(SecurityUtils.hasAnyPermission(Set.of()));
        assertFalse(SecurityUtils.hasAnyRole(Set.of()));
    }
}
