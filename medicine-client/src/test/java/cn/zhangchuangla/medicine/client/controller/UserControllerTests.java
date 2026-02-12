package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.dto.UserProfileDto;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.client.service.UserWalletService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private UserWalletService userWalletService;

    @InjectMocks
    private UserController userController;

    @Test
    void getUserProfile_ShouldReturnProfile() {
        UserProfileDto profile = new UserProfileDto();
        profile.setNickname("测试用户");
        when(userService.getUserProfile()).thenReturn(profile);

        var result = userController.getUserProfile();

        assertEquals(200, result.getCode());
        assertEquals("测试用户", result.getData().getNickname());
        verify(userService).getUserProfile();
    }

    @Test
    void currentUser_ShouldReturnUserWithRoles() {
        User user = new User();
        user.setId(8L);
        user.setUsername("client-user");

        when(userService.getUserById(8L)).thenReturn(user);
        when(userService.getUserRolesByUserId(8L)).thenReturn(Set.of("user"));

        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getUserId).thenReturn(8L);

            var result = userController.currentUser();

            assertEquals(200, result.getCode());
            assertEquals("client-user", result.getData().getUsername());
            assertEquals(Set.of("user"), result.getData().getRoles());
        }

        verify(userService).getUserById(8L);
        verify(userService).getUserRolesByUserId(8L);
    }
}
