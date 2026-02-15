package cn.zhangchuangla.medicine.common.security.token;

import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Set;

import static cn.zhangchuangla.medicine.common.core.constants.SecurityConstants.CLAIM_KEY_SESSION_ID;
import static cn.zhangchuangla.medicine.common.core.constants.SecurityConstants.CLAIM_KEY_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTests {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTokenStore redisTokenStore;

    @Mock
    private ObjectProvider<UserDetailsService> userDetailsServices;

    @Mock
    private UserDetailsService userDetailsService;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void refreshToken_ShouldReloadAuthoritiesFromUserDetails() {
        TokenService tokenService = new TokenService(jwtTokenProvider, redisTokenStore, userDetailsServices);

        Claims claims = Jwts.claims();
        claims.put(CLAIM_KEY_SESSION_ID, "refresh-session-1");
        claims.put(CLAIM_KEY_USERNAME, "alice");
        when(jwtTokenProvider.getClaimsFromToken("refresh-jwt")).thenReturn(claims);
        when(redisTokenStore.isValidRefreshToken("refresh-session-1")).thenReturn(true);
        when(userDetailsServices.iterator()).thenReturn(List.of(userDetailsService).iterator());

        AuthUser authUser = AuthUser.builder()
                .id(5L)
                .username("alice")
                .password("encoded")
                .build();
        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(Set.of(
                new SimpleGrantedAuthority("ROLE_admin"),
                new SimpleGrantedAuthority("system:user:list")
        ));
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);

        when(jwtTokenProvider.createJwt(anyString(), eq("alice"))).thenReturn("new-access-token");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var token = tokenService.refreshToken("refresh-jwt");

        assertEquals("new-access-token", token.getAccessToken());
        assertEquals("refresh-jwt", token.getRefreshToken());

        ArgumentCaptor<String> accessTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTokenStore).mapRefreshTokenToAccessToken(eq("refresh-session-1"), accessTokenCaptor.capture());
        assertFalse(accessTokenCaptor.getValue().isBlank());
    }
}
