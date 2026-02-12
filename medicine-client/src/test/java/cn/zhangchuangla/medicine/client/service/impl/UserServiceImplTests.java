package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserMapper;
import cn.zhangchuangla.medicine.client.service.MallOrderService;
import cn.zhangchuangla.medicine.client.service.UserWalletService;
import cn.zhangchuangla.medicine.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserWalletService userWalletService;

    @Mock
    private MallOrderService mallOrderService;

    @Mock
    private UserMapper userMapper;

    @Spy
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    void getUserRolesByUserId_ShouldFilterBlankAndNullCodes() {
        when(userMapper.listRoleCodesByUserId(1L)).thenReturn(Arrays.asList("admin", " ", null, "user"));

        Set<String> roles = userService.getUserRolesByUserId(1L);

        assertEquals(Set.of("admin", "user"), roles);
        verify(userMapper).listRoleCodesByUserId(1L);
    }

    @Test
    void getUserRolesByUserName_WhenUserNotExist_ShouldReturnEmptySet() {
        doReturn(null).when(userService).getUserByUsername("ghost");

        Set<String> roles = userService.getUserRolesByUserName("ghost");

        assertTrue(roles.isEmpty());
    }

    @Test
    void getUserRolesByUserName_ShouldDelegateToUserIdQuery() {
        User user = new User();
        user.setId(10L);
        doReturn(user).when(userService).getUserByUsername("alice");
        when(userMapper.listRoleCodesByUserId(10L)).thenReturn(List.of("user"));

        Set<String> roles = userService.getUserRolesByUserName("alice");

        assertEquals(Set.of("user"), roles);
        verify(userMapper).listRoleCodesByUserId(10L);
    }
}
