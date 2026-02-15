package cn.zhangchuangla.medicine.common.security.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
}
