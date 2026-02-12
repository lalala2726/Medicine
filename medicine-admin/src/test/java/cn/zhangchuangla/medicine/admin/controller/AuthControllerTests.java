package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.service.AuthService;
import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.model.request.LoginRequest;
import cn.zhangchuangla.medicine.model.vo.CurrentUserInfoVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTests {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ShouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        AuthTokenVo tokenVo = AuthTokenVo.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();
        when(authService.login("admin", "123456")).thenReturn(tokenVo);

        var result = authController.login(request);

        assertEquals(200, result.getCode());
        assertEquals("access-token", result.getData().getAccessToken());
        verify(authService).login("admin", "123456");
    }

    @Test
    void currentUserPermissions_ShouldReturnPermissionSet() {
        when(authService.currentUserPermissions()).thenReturn(Set.of("system:user:list"));

        var result = authController.currentUserPermissions();

        assertEquals(200, result.getCode());
        assertEquals(Set.of("system:user:list"), result.getData());
        verify(authService).currentUserPermissions();
    }

    @Test
    void currentUser_ShouldReturnCurrentUserInfo() {
        CurrentUserInfoVo vo = new CurrentUserInfoVo();
        vo.setId(1L);
        vo.setUsername("admin");
        vo.setRoles(Set.of("super_admin"));
        when(authService.currentUserInfo()).thenReturn(vo);

        var result = authController.currentUser();

        assertEquals(200, result.getCode());
        assertEquals("admin", result.getData().getUsername());
        assertEquals(Set.of("super_admin"), result.getData().getRoles());
        verify(authService).currentUserInfo();
    }
}
