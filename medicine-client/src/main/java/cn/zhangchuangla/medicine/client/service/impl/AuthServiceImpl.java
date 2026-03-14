package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserMapper;
import cn.zhangchuangla.medicine.client.publisher.LoginLogPublisher;
import cn.zhangchuangla.medicine.client.service.AuthService;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.client.task.AsyncUserLogService;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.exception.LoginException;
import cn.zhangchuangla.medicine.common.core.exception.ParamException;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.IpAddressUtils;
import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.common.security.entity.OnlineLoginUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.security.token.JwtTokenProvider;
import cn.zhangchuangla.medicine.common.security.token.RedisTokenStore;
import cn.zhangchuangla.medicine.common.security.token.TokenService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.mq.LoginLogMessage;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Locale;

import static cn.zhangchuangla.medicine.common.core.constants.SecurityConstants.CLAIM_KEY_SESSION_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String LOGIN_TYPE_PASSWORD = "password";
    private static final String LOGIN_SOURCE_CLIENT = "client";

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTokenStore redisTokenStore;
    private final JwtTokenProvider jwtTokenProvider;
    private final AsyncUserLogService asyncUserLogService;
    private final UserMapper userMapper;
    private final LoginLogPublisher loginLogPublisher;

    @Override
    public Long register(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new ParamException("用户名或密码不能为空");
        }
        User exists = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (exists != null) {
            throw new ServiceException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password.trim()));
        userService.save(user);

        Long roleId = userMapper.selectRoleIdByRoleCode(RolesConstant.USER);
        if (roleId == null) {
            throw new ServiceException("默认用户角色不存在，请先初始化RBAC数据");
        }
        userMapper.insertUserRole(user.getId(), roleId);

        return user.getId();
    }

    @Override
    public AuthTokenVo login(String username, String password) {
        Assert.hasText(username, "用户名不能为空");
        Assert.hasText(password, "密码不能为空");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username.trim(),
                password.trim());
        String trimmedUsername = username.trim();
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(token);
            var session = tokenService.createToken(authentication);
            SysUserDetails sysUserDetails = (SysUserDetails) authentication.getPrincipal();
            Long userId = sysUserDetails.getUserId();
            // 记录用户表最后登录信息（保留现有逻辑）
            HttpServletRequest request = resolveRequest();
            String ipAddress = request != null ? IpAddressUtils.getIpAddress(request) : null;
            asyncUserLogService.recordUserLoginLog(userId, ipAddress);
            recordLoginLog(authentication, trimmedUsername, true, null);
            return AuthTokenVo.builder()
                    .accessToken(session.getAccessToken())
                    .refreshToken(session.getRefreshToken())
                    .build();
        } catch (BadCredentialsException e) {
            recordLoginLog(null, trimmedUsername, false, "账号或密码错误");
            throw new LoginException("账号或密码错误");
        } catch (Exception ex) {
            recordLoginLog(authentication, trimmedUsername, false, ex.getMessage());
            throw ex;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public AuthTokenVo refresh(String refreshToken) {
        Assert.hasText(refreshToken, "刷新令牌不能为空");
        return tokenService.refreshToken(refreshToken);
    }

    @Override
    public void logout(String accessToken) {
        Assert.hasText(accessToken, "访问令牌不能为空");

        try {
            // 1. 解析访问令牌获取会话ID
            Claims claims = jwtTokenProvider.getClaimsFromToken(accessToken);
            if (claims == null) {
                log.warn("退出登录时解析访问令牌失败");
                return;
            }

            String accessTokenId = claims.get(CLAIM_KEY_SESSION_ID, String.class);
            if (StringUtils.isBlank(accessTokenId)) {
                log.warn("退出登录时访问令牌中没有会话ID");
                return;
            }

            // 2. 获取在线用户信息
            OnlineLoginUser onlineUser = redisTokenStore.getAccessToken(accessTokenId);
            if (onlineUser != null) {
                String refreshTokenId = onlineUser.getRefreshTokenId();

                // 3. 删除访问令牌和刷新令牌
                redisTokenStore.deleteAccessToken(accessTokenId);
                if (StringUtils.isNotBlank(refreshTokenId)) {
                    redisTokenStore.deleteRefreshToken(refreshTokenId);
                }

                log.info("用户 {} 退出登录成功，已清理会话信息", onlineUser.getUsername());
            } else {
                log.warn("退出登录时未找到在线用户信息，会话ID: {}", accessTokenId);
            }

            // 4. 清空Spring Security上下文
            SecurityContextHolder.clearContext();

        } catch (Exception e) {
            log.error("退出登录发生异常", e);
            // 即使发生异常，也要确保清空Security上下文
            SecurityContextHolder.clearContext();
        }
    }

    private void recordLoginLog(Authentication authentication,
                                String username,
                                boolean success,
                                String failReason) {
        try {
            LoginLogMessage message = new LoginLogMessage();
            message.setUsername(username);
            message.setLoginStatus(success ? 1 : 0);
            message.setFailReason(success ? null : failReason);
            message.setLoginSource(LOGIN_SOURCE_CLIENT);
            message.setLoginType(LOGIN_TYPE_PASSWORD);
            message.setLoginTime(new Date());

            if (authentication != null && authentication.getPrincipal() instanceof SysUserDetails userDetails) {
                message.setUserId(userDetails.getUserId());
                if (StringUtils.isNotBlank(userDetails.getUsername())) {
                    message.setUsername(userDetails.getUsername());
                }
            }

            HttpServletRequest request = resolveRequest();
            if (request != null) {
                String ip = IpAddressUtils.getIpAddress(request);
                message.setIpAddress(ip);
                String userAgent = request.getHeader(USER_AGENT_HEADER);
                message.setUserAgent(userAgent);
                fillUserAgentInfo(message, userAgent);
            }
            loginLogPublisher.publish(message);
        } catch (Exception ex) {
            log.debug("Failed to record client login log", ex);
        }
    }

    private HttpServletRequest resolveRequest() {
        try {
            return SecurityUtils.getHttpServletRequest();
        } catch (Exception ex) {
            return null;
        }
    }

    private void fillUserAgentInfo(LoginLogMessage message, String userAgent) {
        if (!org.springframework.util.StringUtils.hasText(userAgent)) {
            return;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            message.setDeviceType("mobile");
        } else {
            message.setDeviceType("pc");
        }

        if (ua.contains("windows")) {
            message.setOs("Windows");
        } else if (ua.contains("mac os") || ua.contains("macintosh")) {
            message.setOs("macOS");
        } else if (ua.contains("android")) {
            message.setOs("Android");
        } else if (ua.contains("iphone") || ua.contains("ios")) {
            message.setOs("iOS");
        } else if (ua.contains("linux")) {
            message.setOs("Linux");
        } else {
            message.setOs("Unknown");
        }

        if (ua.contains("edg/")) {
            message.setBrowser("Edge");
        } else if (ua.contains("chrome/")) {
            message.setBrowser("Chrome");
        } else if (ua.contains("firefox/")) {
            message.setBrowser("Firefox");
        } else if (ua.contains("safari/") && !ua.contains("chrome/")) {
            message.setBrowser("Safari");
        } else {
            message.setBrowser("Unknown");
        }
    }
}
