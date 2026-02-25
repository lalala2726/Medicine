package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.UserMapper;
import cn.zhangchuangla.medicine.admin.model.dto.UserOrderStatistics;
import cn.zhangchuangla.medicine.admin.model.request.FreezeOrUnUserWalletRequest;
import cn.zhangchuangla.medicine.admin.model.request.WalletChangeRequest;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.common.core.base.Option;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.entity.*;
import cn.zhangchuangla.medicine.model.enums.WalletChangeTypeEnum;
import cn.zhangchuangla.medicine.model.request.UserAddRequest;
import cn.zhangchuangla.medicine.model.request.UserListQueryRequest;
import cn.zhangchuangla.medicine.model.request.UserUpdateRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService, BaseService {

    private final UserWalletLogService userWalletLogService;
    private final MallOrderService mallOrderService;
    private final UserWalletService userWalletService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserDetailDto getUserDetailById(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        User user = getById(userId);
        Assert.notNull(user, "用户不存在");
        UserOrderStatistics userOrderStatistics = mallOrderService.getOrderStatisticsByUserId(userId);
        UserWallet userWallet = userWalletService.lambdaQuery()
                .eq(UserWallet::getUserId, userId)
                .one();

        BigDecimal walletBalance = userWallet == null ? BigDecimal.ZERO : defaultBigDecimal(userWallet.getBalance());
        long totalOrderCount = userOrderStatistics == null || userOrderStatistics.getTotalOrderCount() == null
                ? 0L : userOrderStatistics.getTotalOrderCount();
        BigDecimal totalConsumption = userOrderStatistics == null ? BigDecimal.ZERO
                : defaultBigDecimal(userOrderStatistics.getTotalConsumption());

        UserDetailDto.BasicInfo basicInfo = new UserDetailDto.BasicInfo();
        basicInfo.setUserId(user.getId());
        basicInfo.setRealName(user.getRealName());
        basicInfo.setPhoneNumber(user.getPhoneNumber());
        basicInfo.setEmail(user.getEmail());
        basicInfo.setGender(user.getGender());
        basicInfo.setIdCard(user.getIdCard());

        UserDetailDto.SecurityInfo securityInfo = new UserDetailDto.SecurityInfo();
        securityInfo.setRegisterTime(user.getCreateTime());
        securityInfo.setLastLoginTime(user.getLastLoginTime());
        securityInfo.setLastLoginIp(user.getLastLoginIp());
        securityInfo.setLastLoginLocation(user.getLastLoginLocation());
        securityInfo.setStatus(user.getStatus());

        UserDetailDto detail = new UserDetailDto();
        detail.setAvatar(user.getAvatar());
        detail.setNickName(user.getNickname());
        detail.setWalletBalance(walletBalance);
        detail.setTotalOrders(safeOrderCount(totalOrderCount));
        detail.setTotalConsume(totalConsumption);
        detail.setBasicInfo(basicInfo);
        detail.setSecurityInfo(securityInfo);
        return detail;
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
        return roleService.getUserRoleByUserId(userId);
    }

    /**
     * 根据用户名查询用户角色集合
     *
     * @param username 用户名
     * @return 用户角色集合
     */
    @Override
    public Set<String> getUserRolesByUserName(String username) {
        User user = getUserByUsername(username);
        if (user == null) {
            return Set.of();
        }
        return roleService.getUserRoleByUserId(user.getId());
    }

    /**
     * 获取用户列表
     *
     * @param request 列表查询参数
     * @return 返回用户分页
     */
    @Override
    public Page<UserListDto> listUser(UserListQueryRequest request) {
        Page<User> userPage = request.toPage();
        Page<User> result = baseMapper.listUser(userPage, request);
        List<UserListDto> rows = result.getRecords().stream()
                .map(user -> {
                    UserListDto userListDto = BeanCotyUtils.copyProperties(user, UserListDto.class);
                    String roles = getUserRolesByUserId(user.getId()).stream()
                            .sorted()
                            .collect(Collectors.joining(","));
                    userListDto.setRoles(roles);
                    return userListDto;
                })
                .toList();
        Page<UserListDto> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(rows);
        return dtoPage;
    }

    /**
     * 添加用户
     *
     * @param request 用户添加请求对象
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(UserAddRequest request) {
        // 参数校验
        Assert.notNull(request, "用户添加请求对象不能为空");

        // 转换请求对象为实体对象
        User user = BeanCotyUtils.copyProperties(request, User.class);

        // 加密密码
        String encryptPassword = encryptPassword(request.getPassword());
        user.setPassword(encryptPassword);

        // 保存用户信息
        boolean result = save(user);
        if (!result) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "添加用户失败");
        }

        // 建立用户与角色关联，若前端未传角色则默认分配 user
        Set<Long> roleIds = resolveRoleIdsForCreate(request.getRoles());
        userRoleService.updateUserRole(user.getId(), roleIds);

        // 开通钱包
        userWalletService.openWallet(user.getId());

        return true;
    }

    /**
     * 修改用户
     *
     * @param request 用户修改请求对象
     * @return 修改结果
     */
    @Override
    public boolean updateUser(UserUpdateRequest request) {
        Assert.notNull(request, "用户修改请求对象不能为空");
        Assert.notNull(request.getId(), "用户ID不能为空");
        User user = BeanCotyUtils.copyProperties(request, User.class);

        if (request.getPassword() != null) {
            String password = request.getPassword();
            String encryptPassword = encryptPassword(password);
            user.setPassword(encryptPassword);
        }
        boolean updated = updateById(user);
        if (updated && request.getRoles() != null) {
            roleService.isRoleExistById(request.getRoles());
            userRoleService.updateUserRole(request.getId(), request.getRoles());
        }
        return updated;
    }

    /**
     * 删除用户
     *
     * @param userId 用户id
     * @return 删除结果
     */
    @Override
    public boolean deleteUser(List<Long> userId) {
        Assert.notEmpty(userId, "用户ID不能为空");
        if (userId.contains(RolesConstant.SUPER_ADMIN_USER_ID)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "超级管理员账号禁止删除");
        }
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userId));
        return removeByIds(userId);
    }

    /**
     * 获取用户钱包流水
     *
     * @param request 查询参数
     * @return 用户钱包流水
     */
    @Override
    public Page<UserWalletFlowDto> getUserWalletFlow(Long userId, PageRequest request) {
        Page<UserWalletLog> userWalletFlow = userWalletLogService.getUserWalletFlow(userId, request);
        List<UserWalletFlowDto> walletFlowDtos = new ArrayList<>();

        AtomicLong atomicLong = new AtomicLong(1);
        userWalletFlow.getRecords().forEach(userWalletLog -> {
            // 获取变动类型：1收入、2支出、3冻结、4解冻
            Integer changeType = userWalletLog.getChangeType();
            // 判断是否为收入（使用枚举类的工具方法）
            Boolean isIncome = WalletChangeTypeEnum.isIncome(changeType);

            UserWalletFlowDto walletFlowDto = new UserWalletFlowDto();
            walletFlowDto.setIndex(atomicLong.getAndIncrement());
            walletFlowDto.setAfterBalance(userWalletLog.getAfterBalance());
            walletFlowDto.setAmount(userWalletLog.getAmount());
            walletFlowDto.setBeforeBalance(userWalletLog.getBeforeBalance());
            walletFlowDto.setChangeTime(userWalletLog.getCreatedAt());
            walletFlowDto.setChangeType(userWalletLog.getReason());
            walletFlowDto.setAmountDirection(changeType);
            walletFlowDto.setIsIncome(isIncome);
            walletFlowDtos.add(walletFlowDto);
        });
        Page<UserWalletFlowDto> page = new Page<>(userWalletFlow.getCurrent(), userWalletFlow.getSize(), userWalletFlow.getTotal());
        page.setRecords(walletFlowDtos);
        return page;

    }

    /**
     * 获取用户消费信息
     *
     * @param userId  用户id
     * @param request 查询参数
     * @return 用户消费信息
     */
    @Override
    public Page<UserConsumeInfoDto> getConsumeInfo(Long userId, PageRequest request) {
        Page<MallOrder> mallOrderPage = mallOrderService.getPaidOrderPage(userId, request);
        AtomicLong atomicLong = new AtomicLong(1);
        List<UserConsumeInfoDto> consumeInfoDtos = mallOrderPage.getRecords().stream()
                .map(order -> {
                    UserConsumeInfoDto consumeInfoDto = new UserConsumeInfoDto();
                    consumeInfoDto.setIndex(atomicLong.getAndIncrement());
                    consumeInfoDto.setOrderNo(order.getOrderNo());
                    consumeInfoDto.setPayPrice(order.getPayAmount());
                    consumeInfoDto.setFinishTime(order.getFinishTime());
                    consumeInfoDto.setTotalPrice(order.getTotalAmount());
                    consumeInfoDto.setUserId(order.getUserId());
                    return consumeInfoDto;
                })
                .toList();
        Page<UserConsumeInfoDto> page = new Page<>(mallOrderPage.getCurrent(), mallOrderPage.getSize(), mallOrderPage.getTotal());
        page.setRecords(consumeInfoDtos);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean openUserWallet(Long userId) {
        return userWalletService.openWallet(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeUserWallet(FreezeOrUnUserWalletRequest request) {
        return userWalletService.freezeWallet(request.getUserId(), request.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeUserWallet(FreezeOrUnUserWalletRequest request) {
        return userWalletService.unfreezeWallet(request.getUserId(), request.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean walletAmountChange(WalletChangeRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.notNull(request.getUserId(), "用户ID不能为空");
        Assert.notNull(request.getAmount(), "金额不能为空");
        Assert.isTrue(request.getAmount().compareTo(BigDecimal.ZERO) > 0, "金额必须大于0");
        Assert.notNull(request.getOperationType(), "操作类型不能为空");
        Assert.notEmpty(request.getReason(), "操作原因不能为空");

        User user = getById(request.getUserId());
        if (user == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "用户不存在");
        }

        return switch (request.getOperationType()) {
            case 1 -> userWalletService.rechargeWallet(request.getUserId(), request.getAmount(), request.getReason());
            case 2 -> userWalletService.deductBalance(request.getUserId(), request.getAmount(), request.getReason());
            default ->
                    throw new ServiceException(ResponseCode.PARAM_ERROR, "不支持的操作类型: " + request.getOperationType());
        };
    }

    @Override
    public User getUserById(Long userId) {
        return getById(userId);
    }

    @Override
    public UserWalletDto getUserWallet(Long userId) {
        UserWallet wallet = userWalletService.getUserWalletByUserId(userId);
        return BeanCotyUtils.copyProperties(wallet, UserWalletDto.class);
    }

    @Override
    public List<Option<Long>> listUserOptionsByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        Set<Long> normalized = userIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<User> users = lambdaQuery()
                .select(User::getId, User::getUsername)
                .in(User::getId, normalized)
                .list();
        Map<Long, String> userMap = users.stream()
                .filter(user -> user.getId() != null && StringUtils.isNotBlank(user.getUsername()))
                .collect(Collectors.toMap(User::getId, User::getUsername, (left, right) -> left));
        List<Option<Long>> options = new ArrayList<>();
        for (Long userId : normalized) {
            String username = userMap.get(userId);
            if (username != null) {
                options.add(new Option<>(userId, username));
            }
        }
        return options;
    }


    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int safeOrderCount(long count) {
        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (count < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) count;
    }

    private Set<Long> resolveRoleIdsForCreate(Set<Long> roles) {
        if (roles != null && !roles.isEmpty()) {
            roleService.isRoleExistById(roles);
            return roles;
        }

        Role userRole = roleService.lambdaQuery()
                .eq(Role::getRoleCode, RolesConstant.USER)
                .eq(Role::getStatus, 0)
                .one();
        if (userRole == null || userRole.getId() == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "默认用户角色不存在，请先初始化RBAC数据");
        }
        return Set.of(userRole.getId());
    }

}
