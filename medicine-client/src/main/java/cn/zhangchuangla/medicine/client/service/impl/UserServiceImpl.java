package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserMapper;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.common.core.entity.IPEntity;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.IPUtils;
import cn.zhangchuangla.medicine.model.entity.User;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chuang
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public User getUserById(Long userId) {
        return getById(userId);
    }

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryChainWrapper<User> eq = lambdaQuery().eq(User::getUsername, username);
        return eq.one();
    }

    /**
     * 根据用户ID查询用户角色集合
     *
     * @param userId 用户ID
     * @return 用户角色集合
     */
    @Override
    public Set<String> getUserRolesByUserId(Long userId) {
        LambdaQueryChainWrapper<User> eq = lambdaQuery().eq(User::getId, userId);
        User user = eq.one();
        return extractRoles(user);
    }

    /**
     * 根据用户名查询用户角色集合
     *
     * @param username 用户名
     * @return 用户角色集合
     */
    @Override
    public Set<String> getUserRolesByUserName(String username) {
        LambdaQueryChainWrapper<User> eq = lambdaQuery().eq(User::getUsername, username);
        User user = eq.one();
        return extractRoles(user);
    }

    @Override
    public void updateLoginInfo(Long userId, String ip) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notEmpty(ip, "用户IP不能为空");

        IPEntity regionEntity = IPUtils.getRegionEntity(ip);

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setLastLoginTime(new Date());
        updateUser.setLastLoginIp(ip);
        updateUser.setLastLoginLocation(regionEntity.getRegion());
        updateById(updateUser);
    }

    private Set<String> extractRoles(User user) {
        if (user == null || StringUtils.isBlank(user.getRoles())) {
            return Collections.emptySet();
        }
        String rawRoles = user.getRoles().trim();
        if (rawRoles.startsWith("[") && rawRoles.endsWith("]")) {
            rawRoles = rawRoles.substring(1, rawRoles.length() - 1);
        }
        if (StringUtils.isBlank(rawRoles)) {
            return Collections.emptySet();
        }
        return Arrays.stream(rawRoles.split(","))
                .map(String::trim)
                .map(role -> StringUtils.remove(role, '"'))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }
}




