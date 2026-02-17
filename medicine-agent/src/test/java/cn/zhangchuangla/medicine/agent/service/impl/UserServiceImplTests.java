package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceImplTests {

    @Test
    void getUserPermissionCodesByUserId_ShouldReturnEmpty_WhenUserIdIsNull() {
        UserMapper mapper = createUserMapper(List.of(), List.of(), List.of());
        UserServiceImpl userService = new UserServiceImpl(mapper);

        Set<String> result = userService.getUserPermissionCodesByUserId(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserPermissionCodesByUserId_ShouldReturnAllEnabled_WhenUserIsSuperAdminById() {
        UserMapper mapper = createUserMapper(
                List.of("admin"),
                List.of("mall:product:list"),
                Arrays.asList(" mall:product:list ", "", null, "mall:product:list", "mall:order:list")
        );
        UserServiceImpl userService = new UserServiceImpl(mapper);

        Set<String> result = userService.getUserPermissionCodesByUserId(1L);

        assertEquals(Set.of("mall:product:list", "mall:order:list"), result);
    }

    @Test
    void getUserPermissionCodesByUserId_ShouldReturnAllEnabled_WhenUserHasSuperAdminRole() {
        UserMapper mapper = createUserMapper(
                List.of("admin", "super_admin"),
                List.of("mall:product:list"),
                List.of(" system:user:query ", "mall:product:list")
        );
        UserServiceImpl userService = new UserServiceImpl(mapper);

        Set<String> result = userService.getUserPermissionCodesByUserId(2L);

        assertEquals(Set.of("system:user:query", "mall:product:list"), result);
    }

    @Test
    void getUserPermissionCodesByUserId_ShouldReturnMappedPermissions_WhenUserIsNotSuperAdmin() {
        UserMapper mapper = createUserMapper(
                List.of("admin"),
                Arrays.asList(" mall:product:list ", null, "", "mall:product:list", "mall:order:query"),
                List.of("system:user:query")
        );
        UserServiceImpl userService = new UserServiceImpl(mapper);

        Set<String> result = userService.getUserPermissionCodesByUserId(3L);

        assertEquals(Set.of("mall:product:list", "mall:order:query"), result);
    }

    private UserMapper createUserMapper(List<String> roleCodes,
                                        List<String> permissionCodesByUser,
                                        List<String> allEnabledPermissionCodes) {
        return (UserMapper) Proxy.newProxyInstance(
                UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "listRoleCodesByUserId" -> roleCodes;
                    case "listPermissionCodesByUserId" -> permissionCodesByUser;
                    case "listAllEnabledPermissionCodes" -> allEnabledPermissionCodes;
                    case "toString" -> "UserMapperTestProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }
}
