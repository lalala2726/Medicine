package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.UserMapper;
import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.admin.service.UserWalletLogService;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import cn.zhangchuangla.medicine.model.request.user.UserAddRequest;
import cn.zhangchuangla.medicine.model.request.user.UserListQueryRequest;
import cn.zhangchuangla.medicine.model.request.user.UserUpdateRequest;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chuang
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService, BaseService {

    private final UserWalletLogService userWalletLogService;

    public UserServiceImpl(UserWalletLogService userWalletLogService) {
        this.userWalletLogService = userWalletLogService;
    }

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

    /**
     * 获取用户列表
     *
     * @param request 列表查询参数
     * @return 返回用户分页
     */
    @Override
    public Page<User> listUser(UserListQueryRequest request) {
        Page<User> userPage = request.toPage();
        return baseMapper.listUser(userPage, request);
    }

    /**
     * 添加用户
     *
     * @param request 用户添加请求对象
     * @return 添加结果
     */
    @Override
    public boolean addUser(UserAddRequest request) {
        Assert.notNull(request, "用户添加请求对象不能为空");
        User user = BeanCotyUtils.copyProperties(request, User.class);
        return save(user);
    }

    /**
     * 修改用户
     *
     * @param request 用户修改请求对象
     * @return 修改结果
     */
    @Override
    public boolean updateUser(UserUpdateRequest request) {
        return false;
    }

    /**
     * 删除用户
     *
     * @param userId 用户id
     * @return 删除结果
     */
    @Override
    public boolean deleteUser(List<Long> userId) {
        return removeByIds(userId);
    }

    /**
     * 获取用户钱包流水
     *
     * @param request 查询参数
     * @return 用户钱包流水
     */
    @Override
    public Page<UserWalletLog> getUserWalletFlow(PageRequest request) {
        Long userId = getUserId();
        return userWalletLogService.getUserWalletFlow(userId, request);
    }
}

