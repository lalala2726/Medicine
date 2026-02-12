package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.UserAddRequest;
import cn.zhangchuangla.medicine.model.request.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplRbacTests {

    @Mock
    private UserWalletLogService userWalletLogService;

    @Mock
    private MallOrderService mallOrderService;

    @Mock
    private UserWalletService userWalletService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRoleService userRoleService;

    @Spy
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void addUser_ShouldWriteUserRoleAndOpenWallet() {
        UserAddRequest request = new UserAddRequest();
        request.setUsername("new-user");
        request.setPassword("123456");
        request.setRoles(Set.of(2L, 3L));

        doReturn("ENC-PWD").when(userService).encryptPassword("123456");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(88L);
            return true;
        }).when(userService).save(any(User.class));

        boolean result = userService.addUser(request);

        assertTrue(result);
        verify(roleService).isRoleExistById(Set.of(2L, 3L));
        verify(userRoleService).updateUserRole(88L, Set.of(2L, 3L));
        verify(userWalletService).openWallet(88L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        assertEquals("ENC-PWD", captor.getValue().getPassword());
        assertNull(captor.getValue().getRoles());
    }

    @Test
    void updateUser_WhenContainsRoles_ShouldUpdateUserRoleRelation() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId(9L);
        request.setPassword("abc");
        request.setRoles(Set.of(2L));

        doReturn("ENC-ABC").when(userService).encryptPassword("abc");
        doReturn(true).when(userService).updateById(any(User.class));

        boolean updated = userService.updateUser(request);

        assertTrue(updated);
        verify(roleService).isRoleExistById(Set.of(2L));
        verify(userRoleService).updateUserRole(9L, Set.of(2L));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateById(captor.capture());
        assertNull(captor.getValue().getRoles());
        assertEquals("ENC-ABC", captor.getValue().getPassword());
    }

    @Test
    void deleteUser_WhenContainsSuperAdmin_ShouldThrowException() {
        assertThrows(ServiceException.class,
                () -> userService.deleteUser(List.of(RolesConstant.SUPER_ADMIN_USER_ID)));
    }

    @Test
    void getUserRolesByUserId_ShouldDelegateToRoleService() {
        when(roleService.getUserRoleByUserId(100L)).thenReturn(Set.of("admin"));

        Set<String> roles = userService.getUserRolesByUserId(100L);

        assertEquals(Set.of("admin"), roles);
        verify(roleService).getUserRoleByUserId(100L);
    }
}
