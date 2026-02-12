package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.RoleMapper;
import cn.zhangchuangla.medicine.admin.service.RolePermissionService;
import cn.zhangchuangla.medicine.admin.service.UserRoleService;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTests {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private RolePermissionService rolePermissionService;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void deleteRoleByIds_WhenContainsSuperAdmin_ShouldThrowException() {
        assertThrows(ServiceException.class,
                () -> roleService.deleteRoleByIds(List.of(RolesConstant.SUPER_ADMIN_ROLE_ID, 2L)));
    }

    @Test
    void getUserRoleByUserId_WhenNoRoleRelation_ShouldReturnEmptySet() {
        when(userRoleService.getUserRoleByUserId(100L)).thenReturn(Set.of());

        Set<String> roleCodes = roleService.getUserRoleByUserId(100L);

        assertEquals(Set.of(), roleCodes);
    }

    @Test
    void getRoleIdByUserId_WhenNoRoleRelation_ShouldReturnEmptySet() {
        when(userRoleService.getUserRoleByUserId(100L)).thenReturn(Set.of());

        Set<Long> roleIds = roleService.getRoleIdByUserId(100L);

        assertEquals(Set.of(), roleIds);
    }
}
