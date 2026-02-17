package cn.zhangchuangla.medicine.common.security.token;

import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.common.security.SecurityProperties;
import cn.zhangchuangla.medicine.common.security.entity.OnlineLoginUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisTokenStoreTests {

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @Mock
    @SuppressWarnings("rawtypes")
    private ValueOperations valueOperations;

    @Mock
    private SecurityProperties securityProperties;

    @Test
    void updateAccessTime_ShouldUpdateAccessAndUpdateTime() {
        RedisCache redisCache = new RedisCache(redisTemplate);
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisCache, securityProperties);
        String accessTokenId = "access-1";
        String accessTokenKey = RedisConstants.Auth.USER_ACCESS_TOKEN + accessTokenId;

        OnlineLoginUser onlineLoginUser = OnlineLoginUser.builder()
                .accessTokenId(accessTokenId)
                .createTime(123456L)
                .build();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(accessTokenKey)).thenReturn(onlineLoginUser);
        when(redisTemplate.getExpire(accessTokenKey, TimeUnit.SECONDS)).thenReturn(600L);

        boolean updated = redisTokenStore.updateAccessTime(accessTokenId);

        assertTrue(updated);
        assertEquals(123456L, onlineLoginUser.getCreateTime());
        assertNotNull(onlineLoginUser.getAccessTime());
        assertNotNull(onlineLoginUser.getUpdateTime());
        verify(valueOperations).set(eq(accessTokenKey), eq(onlineLoginUser), eq(600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void updateAccessTime_ShouldBackfillCreateTimeForLegacySession() {
        RedisCache redisCache = new RedisCache(redisTemplate);
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisCache, securityProperties);
        String accessTokenId = "access-legacy";
        String accessTokenKey = RedisConstants.Auth.USER_ACCESS_TOKEN + accessTokenId;

        OnlineLoginUser onlineLoginUser = OnlineLoginUser.builder()
                .accessTokenId(accessTokenId)
                .build();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(accessTokenKey)).thenReturn(onlineLoginUser);
        when(redisTemplate.getExpire(accessTokenKey, TimeUnit.SECONDS)).thenReturn(600L);

        boolean updated = redisTokenStore.updateAccessTime(accessTokenId);

        assertTrue(updated);
        assertNotNull(onlineLoginUser.getCreateTime());
        assertNotNull(onlineLoginUser.getAccessTime());
        assertNotNull(onlineLoginUser.getUpdateTime());
        assertEquals(onlineLoginUser.getCreateTime(), onlineLoginUser.getUpdateTime());
        assertEquals(onlineLoginUser.getCreateTime(), onlineLoginUser.getAccessTime());
        verify(valueOperations).set(eq(accessTokenKey), eq(onlineLoginUser), eq(600L), eq(TimeUnit.SECONDS));
    }
}
