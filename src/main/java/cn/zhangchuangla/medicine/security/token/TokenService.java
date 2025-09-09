package cn.zhangchuangla.medicine.security.token;

import cn.zhangchuangla.medicine.common.exception.AuthorizationException;
import cn.zhangchuangla.medicine.common.utils.IPUtils;
import cn.zhangchuangla.medicine.common.utils.UUIDUtils;
import cn.zhangchuangla.medicine.constants.SecurityConstants;
import cn.zhangchuangla.medicine.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.model.dto.LoginSessionDTO;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.security.entity.OnlineLoginUser;
import cn.zhangchuangla.medicine.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.zhangchuangla.medicine.constants.SecurityConstants.CLAIM_KEY_SESSION_ID;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28 14:19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;
    private final UserService userService;

    public LoginSessionDTO createToken(Authentication authentication) {
        SysUserDetails userDetails = (SysUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        String username = user.getUsername();
        Long id = user.getId();
        String accessTokenSessionId = UUIDUtils.simple();
        String refreshTokenSessionId = UUIDUtils.simple();
        HttpServletRequest httpServletRequest = SecurityUtils.getHttpServletRequest();
        String ipAddress = IPUtils.getIpAddress(httpServletRequest);
        String location = IPUtils.getRegion(ipAddress);

        // 角色集合（可能为空），从用户服务加载
        Set<String> roles = userService.getUserRolesByUserId(id);

        OnlineLoginUser onlineLoginUser = OnlineLoginUser.builder()
                .accessTokenId(accessTokenSessionId)
                .refreshTokenId(refreshTokenSessionId)
                .userId(id)
                .username(username)
                .user(user)
                .roles(roles)
                .ip(ipAddress)
                .location(location)
                .build();

        redisTokenStore.setRefreshToken(refreshTokenSessionId, accessTokenSessionId);
        redisTokenStore.setAccessToken(accessTokenSessionId, onlineLoginUser);
        // JWT 本身不设置过期，由 Redis TTL 判定会话有效性
        String jwtAccessToken = jwtTokenProvider.createJwt(accessTokenSessionId, username);
        String jwtRefreshToken = jwtTokenProvider.createJwt(refreshTokenSessionId, username);

        return LoginSessionDTO.builder()
                .userId(user.getId())
                .accessTokenSessionId(accessTokenSessionId)
                .refreshTokenSessionId(refreshTokenSessionId)
                .username(username)
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    /**
     * 使用JWT刷新令牌刷新访问令牌。
     *
     * @param jwtRefreshToken JWT刷新令牌。
     * @return 新的AuthenticationToken，包含新的JWT访问令牌和原始JWT刷新令牌。
     * @throws AuthorizationException 如果刷新令牌无效或关联的用户会话不存在。
     */
    public AuthTokenVo refreshToken(String jwtRefreshToken) {

        // 先经过JWT验证
        Claims refreshClaims = jwtTokenProvider.getClaimsFromToken(jwtRefreshToken);
        if (refreshClaims == null) {
            throw new AuthorizationException(ResponseResultCode.REFRESH_TOKEN_INVALID);
        }

        String refreshTokenSessionId = refreshClaims.get(CLAIM_KEY_SESSION_ID, String.class);
        if (!redisTokenStore.isValidRefreshToken(refreshTokenSessionId)) {
            throw new AuthorizationException(ResponseResultCode.REFRESH_TOKEN_INVALID);
        }
        String username = refreshClaims.get(SecurityConstants.CLAIM_KEY_USERNAME, String.class);
        // 创建新的访问令牌
        String accessTokenSessionId = UUIDUtils.simple();
        // 刷新后的访问令牌也不设置 exp，由 Redis TTL 管控
        String accessToken = jwtTokenProvider.createJwt(accessTokenSessionId, username);
        // 获取用户角色并构建用户详情对象
        User user = userService.getUserByUsername(username);
        Set<String> roles = userService.getUserRolesByUserId(user.getId());
        HttpServletRequest httpServletRequest = SecurityUtils.getHttpServletRequest();
        String ipAddress = IPUtils.getIpAddress(httpServletRequest);
        String region = IPUtils.getRegion(ipAddress);

        OnlineLoginUser onlineLoginUser = OnlineLoginUser.builder()
                .accessTokenId(accessTokenSessionId)
                .refreshTokenId(refreshTokenSessionId)
                .userId(user.getId())
                .username(username)
                .user(user)
                .roles(roles)
                .ip(ipAddress)
                .location(region)
                .build();

        // 保存刷新令牌
        redisTokenStore.setAccessToken(accessTokenSessionId, onlineLoginUser);
        // 重新设置新的访问令牌和刷新令牌的映射关系
        redisTokenStore.mapRefreshTokenToAccessToken(refreshTokenSessionId, accessTokenSessionId);

        return AuthTokenVo.builder()
                .accessToken(accessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    /**
     * 解析JWT访问令牌获取认证信息。
     * 此方法隐含期望一个访问令牌。
     *
     * @param accessToken JWT访问令牌。
     * @return 用户认证信息（Authentication对象），如果Token无效或解析失败则返回null。
     */
    public Authentication parseAccessToken(String accessToken) {
        // getClaimsFromToken 内部处理异常
        Claims claims = jwtTokenProvider.getClaimsFromToken(accessToken);
        // 如果getClaimsFromToken在无效时返回null而不是抛出异常
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

        // 更新访问时间
        boolean updateSuccess = redisTokenStore.updateAccessTime(accessTokenId);
        if (!updateSuccess) {
            log.warn("更新访问时间失败，令牌可能已被删除: {}", accessTokenId);
            return null;
        }
        log.info("更新访问时间成功: {}", accessTokenId);

        Set<String> userRoles = onlineUser.getRoles() != null ? onlineUser.getRoles() : Collections.emptySet();
        Set<SimpleGrantedAuthority> authorities = userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + role))
                .collect(Collectors.toSet());
        SysUserDetails userDetails = buildUserDetails(onlineUser, authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    /**
     * 构建用户详情对象。
     *
     * @param onlineUser  在线用户信息
     * @param authorities 权限集合
     * @return SysUserDetails 对象
     */
    private SysUserDetails buildUserDetails(OnlineLoginUser onlineUser, Set<SimpleGrantedAuthority> authorities) {
        SysUserDetails userDetails = onlineUser.getUser() != null ? new SysUserDetails(onlineUser.getUser())
                : new SysUserDetails();
        if (onlineUser.getUser() == null) {
            userDetails.setUserId(onlineUser.getUserId());
            userDetails.setUsername(onlineUser.getUsername());
        }
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

}
