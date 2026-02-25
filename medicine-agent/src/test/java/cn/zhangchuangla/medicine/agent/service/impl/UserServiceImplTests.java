package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.dubbo.api.admin.AdminAgentAuthRpcService;
import cn.zhangchuangla.medicine.dubbo.api.client.ClientAgentUserRpcService;
import cn.zhangchuangla.medicine.model.dto.AuthContextDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTests {

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void getUserPermissionCodesByUserId_ShouldReturnEmpty_WhenUserIdIsNull() throws Exception {
        StubAdminAuthRpcService adminAuthRpcService = new StubAdminAuthRpcService();
        UserServiceImpl userService = createService(adminAuthRpcService, new StubClientUserRpcService());

        Set<String> result = userService.getUserPermissionCodesByUserId(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserPermissionCodesByUserId_ShouldNormalizeCodes() throws Exception {
        StubAdminAuthRpcService adminAuthRpcService = new StubAdminAuthRpcService();
        AuthContextDto context = new AuthContextDto();
        context.setPermissions(new LinkedHashSet<>(Arrays.asList(" mall:product:list ", null, "", "mall:product:list", "mall:order:query")));
        adminAuthRpcService.byUserId = context;

        UserServiceImpl userService = createService(adminAuthRpcService, new StubClientUserRpcService());

        Set<String> result = userService.getUserPermissionCodesByUserId(3L);

        assertEquals(Set.of("mall:product:list", "mall:order:query"), result);
    }

    @Test
    void getCurrentUser_ShouldDelegateToClientRpc() throws Exception {
        StubClientUserRpcService clientUserRpcService = new StubClientUserRpcService();
        UserVo userVo = new UserVo();
        userVo.setId(99L);
        clientUserRpcService.userVo = userVo;

        UserServiceImpl userService = createService(new StubAdminAuthRpcService(), clientUserRpcService);

        UserVo current = userService.getCurrentUser(99L);

        assertNotNull(current);
        assertEquals(99L, current.getId());
        assertEquals(99L, clientUserRpcService.capturedUserId);
    }

    @Test
    void getUserByUsername_ShouldReturnUserFromAuthContext() throws Exception {
        StubAdminAuthRpcService adminAuthRpcService = new StubAdminAuthRpcService();
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");

        AuthContextDto context = new AuthContextDto();
        context.setUser(user);
        adminAuthRpcService.byUsername = context;

        UserServiceImpl userService = createService(adminAuthRpcService, new StubClientUserRpcService());

        User result = userService.getUserByUsername("admin");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    private UserServiceImpl createService(StubAdminAuthRpcService adminAuthRpcService,
                                          StubClientUserRpcService clientUserRpcService) throws Exception {
        UserServiceImpl userService = new UserServiceImpl();
        setField(userService, "adminAgentAuthRpcService", adminAuthRpcService);
        setField(userService, "clientAgentUserRpcService", clientUserRpcService);
        return userService;
    }

    private static class StubAdminAuthRpcService implements AdminAgentAuthRpcService {

        private AuthContextDto byUserId;
        private AuthContextDto byUsername;

        @Override
        public AuthContextDto getByUserId(Long userId) {
            return byUserId;
        }

        @Override
        public AuthContextDto getByUsername(String username) {
            return byUsername;
        }
    }

    private static class StubClientUserRpcService implements ClientAgentUserRpcService {

        private Long capturedUserId;
        private UserVo userVo;

        @Override
        public UserVo getCurrentUser(Long userId) {
            this.capturedUserId = userId;
            return userVo;
        }
    }
}
