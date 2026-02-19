package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.mapper.UserMapper;
import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserVo getCurrentUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return BeanCotyUtils.copyProperties(user, UserVo.class);
    }

    @Override
    public AuthUserDto getUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return BeanCotyUtils.copyProperties(user, AuthUserDto.class);
    }

    @Override
    public User getUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public Set<String> getUserRolesByUserId(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return SecurityUtils.normalizeCodes(userMapper.listRoleCodesByUserId(userId));
    }

    @Override
    public Set<String> getUserPermissionCodesByUserId(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        if (RolesConstant.SUPER_ADMIN_USER_ID.equals(userId)) {
            return SecurityUtils.normalizeCodes(userMapper.listAllEnabledPermissionCodes());
        }

        Set<String> roleCodes = getUserRolesByUserId(userId);
        boolean isSuperAdmin = roleCodes.stream()
                .map(SecurityUtils::removeRolePrefix)
                .anyMatch(RolesConstant.SUPER_ADMIN::equalsIgnoreCase);
        if (isSuperAdmin) {
            return SecurityUtils.normalizeCodes(userMapper.listAllEnabledPermissionCodes());
        }
        return SecurityUtils.normalizeCodes(userMapper.listPermissionCodesByUserId(userId));
    }
}
