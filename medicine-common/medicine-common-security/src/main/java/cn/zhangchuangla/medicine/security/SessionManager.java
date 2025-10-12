package cn.zhangchuangla.medicine.security;

import cn.zhangchuangla.medicine.common.core.constants.SecurityConstants;
import cn.zhangchuangla.medicine.common.redis.RedisCache;
import cn.zhangchuangla.medicine.security.entity.OnlineLoginUser;
import cn.zhangchuangla.medicine.security.token.JwtTokenProvider;
import cn.zhangchuangla.medicine.security.token.RedisTokenStore;
import cn.zhangchuangla.medicine.utils.SecurityUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/25 8:31
 */
@Component("sessionManager")
public class SessionManager {


    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenStore redisTokenStore;
    private final RedisCache redisCache;

    public SessionManager(JwtTokenProvider jwtTokenProvider, RedisTokenStore redisTokenStore, RedisCache redisCache) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTokenStore = redisTokenStore;
        this.redisCache = redisCache;
    }

    /**
     * 检查角色
     *
     * @param role 角色
     */
    public boolean checkRole(String role) {
        return SecurityUtils.getRoles().contains(role);
    }

    /**
     * 注销特定用户的登录
     *
     * @param username 用户名
     */
    public boolean logout(String username) {
        // todo 避免使用*进行扫描,后续重新设计相关的结构
        Map<String, Object> map = redisCache.scanKeysWithValues("*");
        map.forEach((key, value) -> {
            OnlineLoginUser onlineLoginUser = (OnlineLoginUser) value;
            if (onlineLoginUser.getUser().getUsername().equals(username)) {
                redisTokenStore.deleteTokenByAccessId(onlineLoginUser.getAccessTokenId());
            }
        });
        return true;
    }

    /**
     * 通过特定刷新令牌注销此用户
     *
     * @param accessToken 访问令牌
     */
    public void logoutByToken(String accessToken) {
        Claims claimsFromToken = jwtTokenProvider.getClaimsFromToken(accessToken);
        String accessTokenId = claimsFromToken.get(SecurityConstants.CLAIM_KEY_SESSION_ID).toString();
        //删除令牌
        redisTokenStore.deleteTokenByAccessId(accessTokenId);
    }

}
