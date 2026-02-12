package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserMapper;
import cn.zhangchuangla.medicine.client.publisher.LoginLogPublisher;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.client.task.AsyncUserLogService;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.security.token.JwtTokenProvider;
import cn.zhangchuangla.medicine.common.security.token.RedisTokenStore;
import cn.zhangchuangla.medicine.common.security.token.TokenService;
import cn.zhangchuangla.medicine.model.dto.LoginSessionDTO;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.mq.LoginLogMessage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RedisTokenStore redisTokenStore;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AsyncUserLogService asyncUserLogService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private LoginLogPublisher loginLogPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @SuppressWarnings("unchecked")
    /**
     * 验证注册时会为新用户绑定默认 user 角色，
     * 并且不再写入旧的 user.roles 字段。
     */
    @Test
    void register_ShouldInsertDefaultUserRole() {
        LambdaQueryChainWrapper<User> query = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(userService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), eq("alice"))).thenReturn(query);
        when(query.one()).thenReturn(null);

        when(passwordEncoder.encode("123456")).thenReturn("ENC");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(66L);
            return true;
        }).when(userService).save(any(User.class));

        when(userMapper.selectRoleIdByRoleCode(RolesConstant.USER)).thenReturn(3L);

        Long userId = authService.register("alice", "123456");

        assertEquals(66L, userId);
        verify(userMapper).insertUserRole(66L, 3L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(userCaptor.capture());
        assertEquals("alice", userCaptor.getValue().getUsername());
        assertEquals("ENC", userCaptor.getValue().getPassword());
        assertNull(userCaptor.getValue().getRoles());
    }

    @SuppressWarnings("unchecked")
    /**
     * 验证默认角色缺失时注册会失败并抛出业务异常，
     * 防止创建没有角色关联的无权限用户。
     */
    @Test
    void register_WhenDefaultRoleMissing_ShouldThrowException() {
        LambdaQueryChainWrapper<User> query = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(userService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), eq("bob"))).thenReturn(query);
        when(query.one()).thenReturn(null);

        when(passwordEncoder.encode("123456")).thenReturn("ENC");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(77L);
            return true;
        }).when(userService).save(any(User.class));

        when(userMapper.selectRoleIdByRoleCode(RolesConstant.USER)).thenReturn(null);

        assertThrows(ServiceException.class, () -> authService.register("bob", "123456"));
    }

    /**
     * 验证客户端登录成功后会发布 client 来源的登录日志，
     * 且保留异步更新最近登录信息的行为。
     */
    @Test
    void login_ShouldPublishClientLoginLog() {
        SysUserDetails userDetails = new SysUserDetails();
        userDetails.setUserId(9L);
        userDetails.setUsername("alice");
        userDetails.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_user")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        LoginSessionDTO session = LoginSessionDTO.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenService.createToken(authentication)).thenReturn(session);

        var result = authService.login("alice", "123456");

        assertEquals("access", result.getAccessToken());
        assertEquals("refresh", result.getRefreshToken());
        verify(asyncUserLogService).recordUserLoginLog(eq(9L), anyString());
        ArgumentCaptor<LoginLogMessage> captor = ArgumentCaptor.forClass(LoginLogMessage.class);
        verify(loginLogPublisher).publish(captor.capture());
        LoginLogMessage message = captor.getValue();
        assertEquals("client", message.getLoginSource());
        assertEquals(1, message.getLoginStatus());
        assertEquals("password", message.getLoginType());
    }
}
