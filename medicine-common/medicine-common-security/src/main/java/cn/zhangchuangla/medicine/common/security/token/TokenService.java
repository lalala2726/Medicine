package cn.zhangchuangla.medicine.common.security.token;

import cn.zhangchuangla.medicine.common.core.constants.SecurityConstants;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.AuthorizationException;
import cn.zhangchuangla.medicine.common.core.utils.IPUtils;
import cn.zhangchuangla.medicine.common.core.utils.UUIDUtils;
import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.OnlineLoginUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.LoginSessionDTO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.zhangchuangla.medicine.common.core.constants.SecurityConstants.CLAIM_KEY_SESSION_ID;

/**
 * Token 颁发与解析服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;
    private final ObjectProvider<UserDetailsService> userDetailsServices;

    /**
     * 基于认证结果创建访问令牌与刷新令牌，并将会话信息写入 Redis。
     *
     * @param authentication Spring Security 认证对象
     * @return 登录会话信息（包含访问令牌与刷新令牌）
     */
    public LoginSessionDTO createToken(Authentication authentication) {
        SysUserDetails userDetails = (SysUserDetails) authentication.getPrincipal();
        AuthUser authUser = userDetails.getUser();
        if (authUser == null) {
            throw new AuthorizationException(ResponseCode.UNAUTHORIZED, "用户信息异常");
        }
        String username = authUser.getUsername();
        Long userId = authUser.getId();

        String accessTokenSessionId = UUIDUtils.simple();
        String refreshTokenSessionId = UUIDUtils.simple();
        HttpServletRequest request = SecurityUtils.getHttpServletRequest();
        String ipAddress = IPUtils.getIpAddress(request);
        String location = IPUtils.getRegion(ipAddress);
        long now = System.currentTimeMillis();

        Set<String> authorityCodes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        RolePermissionSnapshot snapshot = resolveRolePermissionForSession(authUser, authorityCodes);

        OnlineLoginUser onlineLoginUser = OnlineLoginUser.builder()
                .accessTokenId(accessTokenSessionId)
                .refreshTokenId(refreshTokenSessionId)
                .userId(userId)
                .username(username)
                .user(buildSessionUser(authUser))
                .roles(snapshot.roleAuthorities())
                .permissions(snapshot.permissionAuthorities())
                .ip(ipAddress)
                .location(location)
                .createTime(now)
                .updateTime(now)
                .accessTime(now)
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

    /**
     * 使用刷新令牌换发新的访问令牌，并更新 Redis 中的访问会话。
     *
     * @param jwtRefreshToken 刷新令牌
     * @return 新的访问令牌与原刷新令牌
     */
    public AuthTokenVo refreshToken(String jwtRefreshToken) {
        Claims refreshClaims = jwtTokenProvider.getClaimsFromToken(jwtRefreshToken);
        if (refreshClaims == null) {
            throw new AuthorizationException(ResponseCode.REFRESH_TOKEN_INVALID);
        }

        String refreshTokenSessionId = refreshClaims.get(CLAIM_KEY_SESSION_ID, String.class);
        if (!redisTokenStore.isValidRefreshToken(refreshTokenSessionId)) {
            throw new AuthorizationException(ResponseCode.REFRESH_TOKEN_INVALID);
        }

        String username = refreshClaims.get(SecurityConstants.CLAIM_KEY_USERNAME, String.class);
        SysUserDetails userDetails = loadUserDetails(username);
        AuthUser authUser = userDetails.getUser();
        if (authUser == null) {
            throw new AuthorizationException(ResponseCode.REFRESH_TOKEN_INVALID, "无法加载用户信息");
        }
        Set<String> authorityCodes = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        RolePermissionSnapshot snapshot = resolveRolePermissionForSession(authUser, authorityCodes);

        String accessTokenSessionId = UUIDUtils.simple();
        String accessToken = jwtTokenProvider.createJwt(accessTokenSessionId, username);

        HttpServletRequest request = SecurityUtils.getHttpServletRequest();
        String ipAddress = IPUtils.getIpAddress(request);
        String region = IPUtils.getRegion(ipAddress);
        long now = System.currentTimeMillis();

        OnlineLoginUser onlineLoginUser = OnlineLoginUser.builder()
                .accessTokenId(accessTokenSessionId)
                .refreshTokenId(refreshTokenSessionId)
                .userId(authUser.getId())
                .username(username)
                .user(buildSessionUser(authUser))
                .roles(snapshot.roleAuthorities())
                .permissions(snapshot.permissionAuthorities())
                .ip(ipAddress)
                .location(region)
                .createTime(now)
                .updateTime(now)
                .accessTime(now)
                .build();

        redisTokenStore.setAccessToken(accessTokenSessionId, onlineLoginUser);
        redisTokenStore.mapRefreshTokenToAccessToken(refreshTokenSessionId, accessTokenSessionId);

        return AuthTokenVo.builder()
                .accessToken(accessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    /**
     * 根据用户名加载统一用户详情对象。
     *
     * @param username 用户名
     * @return 标准化用户详情
     */
    private SysUserDetails loadUserDetails(String username) {
        UserDetailsService userDetailsService = resolveUserDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails instanceof SysUserDetails sysUserDetails) {
            return sysUserDetails;
        }
        throw new AuthorizationException(ResponseCode.REFRESH_TOKEN_INVALID, "无法加载用户信息");
    }

    /**
     * 解析并选择唯一的 {@link UserDetailsService} 实现。
     *
     * @return 用户详情服务
     */
    private UserDetailsService resolveUserDetailsService() {
        var iterator = userDetailsServices.iterator();
        if (!iterator.hasNext()) {
            throw new AuthorizationException(ResponseCode.REFRESH_TOKEN_INVALID, "未配置用户详情服务");
        }
        UserDetailsService service = iterator.next();
        if (iterator.hasNext()) {
            throw new AuthorizationException(ResponseCode.REFRESH_TOKEN_INVALID, "存在多个用户详情服务，无法确定刷新令牌使用的实现");
        }
        return service;
    }

    /**
     * 解析访问令牌并从 Redis 会话恢复 Spring Security 认证信息。
     *
     * @param accessToken 访问令牌
     * @return 认证对象；无效时返回 {@code null}
     */
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

        RolePermissionSnapshot snapshot = resolveRolePermissionFromSession(onlineUser);
        Set<SimpleGrantedAuthority> authorities = Stream.concat(
                        snapshot.roleAuthorities().stream(),
                        snapshot.permissionAuthorities().stream())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        SysUserDetails userDetails = buildUserDetails(
                onlineUser,
                authorities,
                snapshot.roleAuthorities(),
                snapshot.permissionAuthorities());
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    /**
     * 基于 Redis 在线会话构造 {@link SysUserDetails}。
     *
     * @param onlineUser Redis 中的在线用户
     * @param authorities 已计算的权限集合
     * @param roleAuthorities 角色权限（ROLE_ 前缀）
     * @param permissionAuthorities 业务权限码
     * @return 统一用户详情
     */
    private SysUserDetails buildUserDetails(OnlineLoginUser onlineUser,
                                            Set<SimpleGrantedAuthority> authorities,
                                            Set<String> roleAuthorities,
                                            Set<String> permissionAuthorities) {
        AuthUser authUser = onlineUser.getUser();
        Set<String> roleCodes = toRoleCodes(roleAuthorities);
        if (authUser == null) {
            authUser = AuthUser.builder()
                    .id(onlineUser.getUserId())
                    .username(onlineUser.getUsername())
                    .roles(roleCodes)
                    .permissions(permissionAuthorities)
                    .build();
        } else {
            authUser.setRoles(roleCodes);
            authUser.setPermissions(permissionAuthorities);
        }
        SysUserDetails userDetails = new SysUserDetails(authUser);
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

    /**
     * 构建用于 Redis 会话存储的用户信息快照。
     * <p>
     * 该快照仅保留基础用户字段。
     * 会话角色与权限统一由 {@link OnlineLoginUser#roles}/{@link OnlineLoginUser#permissions}
     * 作为单一来源进行存储，密码字段不写入 Redis。
     * </p>
     *
     * @param authUser 运行期认证用户
     * @return 适用于会话持久化的裁剪用户对象
     */
    private AuthUser buildSessionUser(AuthUser authUser) {
        return AuthUser.builder()
                .id(authUser.getId())
                .username(authUser.getUsername())
                .password(null)
                .status(authUser.getStatus())
                .roles(Set.of())
                .permissions(Set.of())
                .enabled(authUser.isEnabled())
                .accountNonLocked(authUser.isAccountNonLocked())
                .accountNonExpired(authUser.isAccountNonExpired())
                .credentialsNonExpired(authUser.isCredentialsNonExpired())
                .attributes(authUser.getAttributes())
                .createdAt(authUser.getCreatedAt())
                .updatedAt(authUser.getUpdatedAt())
                .build();
    }

    /**
     * 解析待写入会话的角色与权限快照。
     * <p>
     * 优先使用 {@link AuthUser} 中的角色/权限；若缺失则从 authorities 回退拆分。
     * </p>
     *
     * @param authUser 统一用户对象
     * @param authorityCodes 认证对象中的权限编码
     * @return 角色与权限快照
     */
    private RolePermissionSnapshot resolveRolePermissionForSession(AuthUser authUser, Set<String> authorityCodes) {
        Set<String> roleAuthorities = toRoleAuthorities(authUser == null ? Collections.emptySet() : authUser.getRoles());
        Set<String> permissionAuthorities = toPermissionAuthorities(
                authUser == null ? Collections.emptySet() : authUser.getPermissions());
        if (!authorityCodes.isEmpty()) {
            RolePermissionSnapshot authoritySnapshot = splitAuthorities(authorityCodes);
            if (roleAuthorities.isEmpty()) {
                roleAuthorities = authoritySnapshot.roleAuthorities();
            }
            if (permissionAuthorities.isEmpty()) {
                permissionAuthorities = authoritySnapshot.permissionAuthorities();
            }
        }
        return new RolePermissionSnapshot(roleAuthorities, permissionAuthorities);
    }

    /**
     * 从 Redis 会话恢复角色与权限快照。
     * <p>
     * 优先读取会话中的 permissions；若旧会话缺失 permissions，则从混合 roles 回退拆分。
     * </p>
     *
     * @param onlineUser Redis 在线会话
     * @return 角色与权限快照
     */
    private RolePermissionSnapshot resolveRolePermissionFromSession(OnlineLoginUser onlineUser) {
        RolePermissionSnapshot roleSnapshot = splitAuthorities(onlineUser.getRoles());
        Set<String> permissions = toPermissionAuthorities(onlineUser.getPermissions());
        if (permissions.isEmpty()) {
            permissions = roleSnapshot.permissionAuthorities();
        } else if (!roleSnapshot.permissionAuthorities().isEmpty()) {
            LinkedHashSet<String> merged = new LinkedHashSet<>(permissions);
            merged.addAll(roleSnapshot.permissionAuthorities());
            permissions = Set.copyOf(merged);
        }
        return new RolePermissionSnapshot(roleSnapshot.roleAuthorities(), permissions);
    }

    /**
     * 将混合权限集合拆分为角色权限与业务权限。
     *
     * @param authorityCodes 混合权限编码集合
     * @return 角色与权限快照
     */
    private RolePermissionSnapshot splitAuthorities(Set<String> authorityCodes) {
        LinkedHashSet<String> roleAuthorities = new LinkedHashSet<>();
        LinkedHashSet<String> permissionAuthorities = new LinkedHashSet<>();
        for (String authority : normalizeCodes(authorityCodes)) {
            if (isRoleAuthority(authority)) {
                String roleAuthority = toRoleAuthority(authority);
                if (roleAuthority != null) {
                    roleAuthorities.add(roleAuthority);
                }
                continue;
            }
            permissionAuthorities.add(authority);
        }
        return new RolePermissionSnapshot(Set.copyOf(roleAuthorities), Set.copyOf(permissionAuthorities));
    }

    /**
     * 将角色编码集合标准化为 ROLE_ 前缀权限集合。
     *
     * @param roleCodes 角色编码集合
     * @return 角色权限集合
     */
    private Set<String> toRoleAuthorities(Set<String> roleCodes) {
        LinkedHashSet<String> roleAuthorities = new LinkedHashSet<>();
        for (String roleCode : normalizeCodes(roleCodes)) {
            String roleAuthority = toRoleAuthority(roleCode);
            if (roleAuthority != null) {
                roleAuthorities.add(roleAuthority);
            }
        }
        if (roleAuthorities.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(roleAuthorities);
    }

    /**
     * 将权限编码集合标准化为业务权限集合（排除 ROLE_ 项）。
     *
     * @param permissions 权限编码集合
     * @return 业务权限集合
     */
    private Set<String> toPermissionAuthorities(Set<String> permissions) {
        LinkedHashSet<String> permissionAuthorities = new LinkedHashSet<>();
        for (String permission : normalizeCodes(permissions)) {
            if (!isPrefixedRoleAuthority(permission)) {
                permissionAuthorities.add(permission);
            }
        }
        if (permissionAuthorities.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(permissionAuthorities);
    }

    /**
     * 将 ROLE_ 前缀角色权限转换为纯角色编码。
     *
     * @param roleAuthorities 角色权限集合
     * @return 角色编码集合
     */
    private Set<String> toRoleCodes(Set<String> roleAuthorities) {
        LinkedHashSet<String> roleCodes = new LinkedHashSet<>();
        for (String roleAuthority : normalizeCodes(roleAuthorities)) {
            String roleCode = removeRolePrefix(roleAuthority);
            if (!roleCode.isEmpty()) {
                roleCodes.add(roleCode);
            }
        }
        if (roleCodes.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(roleCodes);
    }

    /**
     * 归一化编码集合：去空、去空白、去重。
     *
     * @param values 待归一化集合
     * @return 归一化后的不可变集合
     */
    private Set<String> normalizeCodes(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        if (normalized.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(normalized);
    }

    /**
     * 将角色编码转换为 ROLE_ 前缀权限。
     *
     * @param roleCode 角色编码或角色权限
     * @return ROLE_ 前缀权限；为空时返回 {@code null}
     */
    private String toRoleAuthority(String roleCode) {
        String normalizedRoleCode = removeRolePrefix(roleCode);
        if (normalizedRoleCode.isEmpty()) {
            return null;
        }
        return ROLE_PREFIX + normalizedRoleCode;
    }

    /**
     * 去除角色权限前缀 {@code ROLE_}。
     *
     * @param roleOrAuthority 角色编码或角色权限
     * @return 去前缀后的角色编码
     */
    private String removeRolePrefix(String roleOrAuthority) {
        if (roleOrAuthority == null) {
            return "";
        }
        String trimmed = roleOrAuthority.trim();
        if (trimmed.regionMatches(true, 0, ROLE_PREFIX, 0, ROLE_PREFIX.length())) {
            return trimmed.substring(ROLE_PREFIX.length()).trim();
        }
        return trimmed;
    }

    /**
     * 判断编码是否应视为角色项。
     *
     * @param authority 待判断编码
     * @return true 表示角色项
     */
    private boolean isRoleAuthority(String authority) {
        if (isPrefixedRoleAuthority(authority)) {
            return true;
        }
        return !looksLikePermissionAuthority(authority);
    }

    /**
     * 判断编码是否带有角色前缀。
     *
     * @param authority 编码
     * @return true 表示带有 ROLE_ 前缀
     */
    private boolean isPrefixedRoleAuthority(String authority) {
        return authority.regionMatches(true, 0, ROLE_PREFIX, 0, ROLE_PREFIX.length());
    }

    /**
     * 判断编码是否更像业务权限（包含分隔符）。
     *
     * @param authority 编码
     * @return true 表示业务权限风格编码
     */
    private boolean looksLikePermissionAuthority(String authority) {
        return authority.contains(":") || authority.contains(".") || authority.contains("/");
    }

    private record RolePermissionSnapshot(Set<String> roleAuthorities, Set<String> permissionAuthorities) {
    }
}
