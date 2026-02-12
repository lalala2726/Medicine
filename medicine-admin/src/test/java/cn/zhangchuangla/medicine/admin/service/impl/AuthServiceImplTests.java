package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.service.PermissionService;
import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.exception.LoginException;
import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.common.security.token.JwtTokenProvider;
import cn.zhangchuangla.medicine.common.security.token.RedisTokenStore;
import cn.zhangchuangla.medicine.common.security.token.TokenService;
import cn.zhangchuangla.medicine.model.dto.LoginSessionDTO;
import cn.zhangchuangla.medicine.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private RedisTokenStore redisTokenStore;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private PermissionService permissionService;

    @Spy
    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_WhenHasAdminRole_ShouldReturnToken() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_" + RolesConstant.ADMIN))
        );
        LoginSessionDTO loginSessionDTO = LoginSessionDTO.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenService.createToken(authentication)).thenReturn(loginSessionDTO);

        AuthTokenVo token = authService.login("admin", "123456");

        assertEquals("access", token.getAccessToken());
        assertEquals("refresh", token.getRefreshToken());
        verify(tokenService).createToken(authentication);
    }

    @Test
    void login_WhenNoAdminRole_ShouldThrowLoginException() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_user"))
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        assertThrows(LoginException.class, () -> authService.login("user", "123456"));
    }

    @Test
    void currentUserInfo_ShouldReturnUserWithRoles() {
        User user = new User();
        user.setId(10L);
        user.setUsername("admin");

        doReturn(10L).when(authService).getUserId();
        when(userService.getUserById(10L)).thenReturn(user);
        when(userService.getUserRolesByUserId(10L)).thenReturn(Set.of("admin", "super_admin"));

        var vo = authService.currentUserInfo();

        assertEquals(10L, vo.getId());
        assertEquals(Set.of("admin", "super_admin"), vo.getRoles());
        verify(userService).getUserById(10L);
        verify(userService).getUserRolesByUserId(10L);
    }

    @Test
    void currentUserPermissions_ShouldDelegateToPermissionService() {
        doReturn(7L).when(authService).getUserId();
        when(permissionService.getPermissionCodesByUserId(7L)).thenReturn(Set.of("system:user:list"));

        var permissions = authService.currentUserPermissions();

        assertEquals(Set.of("system:user:list"), permissions);
        verify(permissionService).getPermissionCodesByUserId(7L);
    }
}
