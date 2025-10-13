package cn.zhangchuangla.medicine.common.security.token;

import cn.zhangchuangla.medicine.common.core.constants.SecurityConstants;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.AuthorizationException;
import cn.zhangchuangla.medicine.common.core.utils.IPUtils;
import cn.zhangchuangla.medicine.common.core.utils.UUIDUtils;
import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.OnlineLoginUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.security.spi.SecurityUserService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.LoginSessionDTO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.zhangchuangla.medicine.common.core.constants.SecurityConstants.CLAIM_KEY_SESSION_ID;

/**
 * Token 颁发与解析服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;
    private final SecurityUserService securityUserService;

    public LoginSessionDTO createToken(Authentication authentication) {
        SysUserDetails userDetails = (SysUserDetails) authentication.getPrincipal();
        AuthUser authUser = userDetails.getUser();
        if (authUser == null) {
            throw new AuthorizationException(ResponseResultCode.UNAUTHORIZED, "用户信息异常");
        }
        String username = authUser.getUsername();
        Long userId = authUser.getId();

        String accessTokenSessionId = UUIDUtils.simple();
        String refreshTokenSessionId = UUIDUtils.simple();
        HttpServletRequest request = SecurityUtils.getHttpServletRequest();
        String ipAddress = IPUtils.getIpAddress(request);
        String location = IPUtils.getRegion(ipAddress);

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> StringUtils.removeStart(authority, SecurityConstants.ROLE_PREFIX))
                .collect(Collectors.toSet());

        OnlineLoginUser onlineLoginUser = OnlineLoginUser.builder()
                .accessTokenId(accessTokenSessionId)
                .refreshTokenId(refreshTokenSessionId)
                .userId(userId)
                .username(username)
                .user(authUser)
                .roles(roles)
                .ip(ipAddress)
                .location(location)
                .build();

        redisTokenStore.setRefreshToken(refreshTokenSessionId, accessTokenSessionId);
        redisTokenStore.setAccessToken(accessTokenSessionId, onlineLoginUser);

        String jwtAccessToken = jwtTokenProvider.createJwt(accessTokenSessionId, username);
        String jwtRefreshToken = jwtTokenProvider.createJwt(refreshTokenSessionId, username);

        return LoginSessionDTO.builder()
                .userId(userId)
                .accessTokenSessionId(accessTokenSessionId)
                .refreshTokenSessionId(refreshTokenSessionId)
                .username(username)
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    public AuthTokenVo refreshToken(String jwtRefreshToken) {
        Claims refreshClaims = jwtTokenProvider.getClaimsFromToken(jwtRefreshToken);
        if (refreshClaims == null) {
            throw new AuthorizationException(ResponseResultCode.REFRESH_TOKEN_INVALID);
        }

        String refreshTokenSessionId = refreshClaims.get(CLAIM_KEY_SESSION_ID, String.class);
        if (!redisTokenStore.isValidRefreshToken(refreshTokenSessionId)) {
            throw new AuthorizationException(ResponseResultCode.REFRESH_TOKEN_INVALID);
        }

        String username = refreshClaims.get(SecurityConstants.CLAIM_KEY_USERNAME, String.class);
        AuthUser authUser = securityUserService.loadUserByUsername(username)
                .orElseThrow(() -> new AuthorizationException(ResponseResultCode.REFRESH_TOKEN_INVALID));

        String accessTokenSessionId = UUIDUtils.simple();
        String accessToken = jwtTokenProvider.createJwt(accessTokenSessionId, username);

        HttpServletRequest request = SecurityUtils.getHttpServletRequest();
        String ipAddress = IPUtils.getIpAddress(request);
        String region = IPUtils.getRegion(ipAddress);

        OnlineLoginUser onlineLoginUser = OnlineLoginUser.builder()
                .accessTokenId(accessTokenSessionId)
                .refreshTokenId(refreshTokenSessionId)
                .userId(authUser.getId())
                .username(username)
                .user(authUser)
                .roles(authUser.getRoles())
                .ip(ipAddress)
                .location(region)
                .build();

        redisTokenStore.setAccessToken(accessTokenSessionId, onlineLoginUser);
        redisTokenStore.mapRefreshTokenToAccessToken(refreshTokenSessionId, accessTokenSessionId);

        return AuthTokenVo.builder()
                .accessToken(accessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    public Authentication parseAccessToken(String accessToken) {
        Claims claims = jwtTokenProvider.getClaimsFromToken(accessToken);
        if (claims == null) {
            log.warn("解析访问令牌失败或Claims为空: {}", accessToken);
            return null;
        }

        String accessTokenId = claims.get(CLAIM_KEY_SESSION_ID, String.class);
        if (StringUtils.isBlank(accessTokenId)) {
            log.warn("访问令牌JWT中未找到sessionId ({}): {}", CLAIM_KEY_SESSION_ID, accessToken);
            return null;
        }

        OnlineLoginUser onlineUser = redisTokenStore.getAccessToken(accessTokenId);
        if (onlineUser == null) {
            log.warn("Redis 中未找到访问令牌: {}", accessTokenId);
            return null;
        }

        boolean updateSuccess = redisTokenStore.updateAccessTime(accessTokenId);
        if (!updateSuccess) {
            log.warn("更新访问时间失败，令牌可能已被删除: {}", accessTokenId);
            return null;
        }

        Set<String> userRoles = onlineUser.getRoles() != null ? onlineUser.getRoles() : Collections.emptySet();
        Set<SimpleGrantedAuthority> authorities = userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + role))
                .collect(Collectors.toSet());

        SysUserDetails userDetails = buildUserDetails(onlineUser, authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    private SysUserDetails buildUserDetails(OnlineLoginUser onlineUser, Set<SimpleGrantedAuthority> authorities) {
        AuthUser authUser = onlineUser.getUser();
        if (authUser == null) {
            Set<String> roles = onlineUser.getRoles() != null ? onlineUser.getRoles() : Collections.emptySet();
            authUser = AuthUser.builder()
                    .id(onlineUser.getUserId())
                    .username(onlineUser.getUsername())
                    .roles(roles)
                    .build();
        }
        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(authorities);
        return userDetails;
    }
}
