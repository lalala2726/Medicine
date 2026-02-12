package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.PermissionMapper;
import cn.zhangchuangla.medicine.admin.model.request.RolePermissionUpdateRequest;
import cn.zhangchuangla.medicine.model.entity.Permission;
import cn.zhangchuangla.medicine.model.entity.RolePermission;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceImplTests {

    @Mock
    private PermissionMapper permissionMapper;

    @Spy
    @InjectMocks
    private RolePermissionServiceImpl rolePermissionService;

    @SuppressWarnings("unchecked")
    @Test
    void getRolePermission_ShouldDistinctPermissionIds() {
        LambdaQueryChainWrapper<RolePermission> wrapper = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        RolePermission rp1 = new RolePermission();
        rp1.setPermissionId(10L);
        RolePermission rp2 = new RolePermission();
        rp2.setPermissionId(10L);
        RolePermission rp3 = new RolePermission();
        rp3.setPermissionId(20L);

        doReturn(wrapper).when(rolePermissionService).lambdaQuery();
        doReturn(wrapper).when(wrapper).in(any(), anyCollection());
        when(wrapper.list()).thenReturn(List.of(rp1, rp2, rp3));

        List<Long> result = rolePermissionService.getRolePermission(List.of(1L, 2L));

        assertEquals(List.of(10L, 20L), result);
    }

    @Test
    void updateRolePermission_ShouldFilterNonExistingPermissionIds() {
        RolePermissionUpdateRequest request = new RolePermissionUpdateRequest();
        request.setRoleId(1L);
        request.setPermissionIds(List.of(11L, 22L));

        Permission permission = new Permission();
        permission.setId(22L);

        doReturn(true).when(rolePermissionService).remove(any());
        when(permissionMapper.selectBatchIds(List.of(11L, 22L))).thenReturn(List.of(permission));
        doReturn(true).when(rolePermissionService).saveBatch(any());

        boolean updated = rolePermissionService.updateRolePermission(request);

        assertTrue(updated);
        ArgumentCaptor<List<RolePermission>> captor = ArgumentCaptor.forClass(List.class);
        verify(rolePermissionService).saveBatch(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(1L, captor.getValue().getFirst().getRoleId());
        assertEquals(22L, captor.getValue().getFirst().getPermissionId());
    }

    @Test
    void updateRolePermission_WhenPermissionIdsEmpty_ShouldOnlyClearRelation() {
        RolePermissionUpdateRequest request = new RolePermissionUpdateRequest();
        request.setRoleId(1L);
        request.setPermissionIds(List.of());
        doReturn(true).when(rolePermissionService).remove(any());

        boolean updated = rolePermissionService.updateRolePermission(request);

        assertTrue(updated);
        verify(rolePermissionService).remove(any());
    }
}
