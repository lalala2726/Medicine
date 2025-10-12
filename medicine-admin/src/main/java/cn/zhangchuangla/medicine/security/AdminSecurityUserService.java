package cn.zhangchuangla.medicine.security;

import cn.zhangchuangla.medicine.common.core.constants.Constants;
import cn.zhangchuangla.medicine.common.core.model.entity.User;
import cn.zhangchuangla.medicine.security.entity.AuthUser;
import cn.zhangchuangla.medicine.security.spi.SecurityUserService;
import cn.zhangchuangla.medicine.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 管理端的用户查询实现，负责将业务用户转换为通用的安全模型。
 */
@Service
@RequiredArgsConstructor
public class AdminSecurityUserService implements SecurityUserService {

    private final UserService userService;

    @Override
    public Optional<AuthUser> loadUserByUsername(String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Optional.empty();
        }
        Set<String> roles = Optional.ofNullable(userService.getUserRolesByUserId(user.getId()))
                .filter(set -> !set.isEmpty())
                .orElseGet(java.util.Collections::emptySet);
        boolean unlocked = Objects.equals(user.getStatus(), Constants.ACCOUNT_UNLOCK_KEY);
        AuthUser authUser = AuthUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .status(user.getStatus())
                .roles(roles)
                .enabled(unlocked)
                .accountNonLocked(unlocked)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();
        return Optional.of(authUser);
    }
}
