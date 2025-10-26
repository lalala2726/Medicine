package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.service.AuthService;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.exception.AccessDeniedException;
import cn.zhangchuangla.medicine.common.core.exception.LoginException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.common.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.common.security.entity.OnlineLoginUser;
import cn.zhangchuangla.medicine.common.security.token.JwtTokenProvider;
import cn.zhangchuangla.medicine.common.security.token.RedisTokenStore;
import cn.zhangchuangla.medicine.common.security.token.TokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

import static cn.zhangchuangla.medicine.common.core.constants.SecurityConstants.CLAIM_KEY_SESSION_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService, BaseService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RedisTokenStore redisTokenStore;
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public AuthTokenVo login(String username, String password) {
        Assert.hasText(username, "用户名不能为空");
        Assert.hasText(password, "密码不能为空");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username.trim(),
                password.trim());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        } catch (BadCredentialsException e) {
            throw new LoginException("账号或密码错误");
        } finally {
            SecurityContextHolder.clearContext();
        }

        Set<String> roles = getRoles();
        if (!roles.contains(RolesConstant.ADMIN)) {
            throw new AccessDeniedException("无权限访问");
        }
        // 生成会话令牌
        var session = tokenService.createToken(authentication);
        return AuthTokenVo.builder()
                .accessToken(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .build();
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
}
