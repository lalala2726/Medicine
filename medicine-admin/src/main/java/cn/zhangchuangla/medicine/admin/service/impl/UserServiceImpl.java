package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.UserMapper;
import cn.zhangchuangla.medicine.admin.model.dto.UserOrderStatistics;
import cn.zhangchuangla.medicine.admin.model.request.FreezeOrUnUserWalletRequest;
import cn.zhangchuangla.medicine.admin.model.request.WalletChangeRequest;
import cn.zhangchuangla.medicine.admin.model.vo.UserConsumeInfo;
import cn.zhangchuangla.medicine.admin.model.vo.UserDetailVo;
import cn.zhangchuangla.medicine.admin.model.vo.UserWalletFlowInfoVo;
import cn.zhangchuangla.medicine.admin.model.vo.UserWalletVo;
import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.admin.service.UserWalletLogService;
import cn.zhangchuangla.medicine.admin.service.UserWalletService;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.constants.RolesConstant;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.entity.UserWallet;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import cn.zhangchuangla.medicine.model.request.user.UserAddRequest;
import cn.zhangchuangla.medicine.model.request.user.UserListQueryRequest;
import cn.zhangchuangla.medicine.model.request.user.UserUpdateRequest;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService, BaseService {

    private final UserWalletLogService userWalletLogService;
    private final MallOrderService mallOrderService;
    private final UserWalletService userWalletService;

    public UserServiceImpl(UserWalletLogService userWalletLogService, MallOrderService mallOrderService, UserWalletService userWalletService) {
        this.userWalletLogService = userWalletLogService;
        this.mallOrderService = mallOrderService;
        this.userWalletService = userWalletService;
    }

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserDetailVo getUserDetailById(Long userId) {
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

        UserDetailVo.BasicInfo basicInfo = UserDetailVo.BasicInfo.builder()
                .userId(user.getId())
                .realName(user.getRealName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .gender(user.getGender())
                .idCard(user.getIdCard())
                .build();

        UserDetailVo.SecurityInfo securityInfo = UserDetailVo.SecurityInfo.builder()
                .registerTime(user.getCreateTime())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .lastLoginLocation(user.getLastLoginLocation())
                .status(user.getStatus())
                .build();

        return UserDetailVo.builder()
                .avatar(user.getAvatar())
                .nickName(user.getNickname())
                .walletBalance(walletBalance)
                .totalOrders(safeOrderCount(totalOrderCount))
                .totalConsume(totalConsumption)
                .basicInfo(basicInfo)
                .securityInfo(securityInfo)
                .build();
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
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(UserAddRequest request) {
        // 参数校验
        Assert.notNull(request, "用户添加请求对象不能为空");

        // 转换请求对象为实体对象
        User user = BeanCotyUtils.copyProperties(request, User.class);

        // 检查角色是否合法
        if (!request.getRoles().isEmpty()) {
            // 检查角色是否都是有效的角色
            boolean hasInvalidRole = request.getRoles().stream()
                    .anyMatch(role -> !RolesConstant.ADMIN.equals(role) && !RolesConstant.USER.equals(role));
            if (hasInvalidRole) {
                throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "角色系统不存在");
            }
        }

        // 设置默认角色
        if (request.getRoles().isEmpty()) {
            user.setRoles("[" + RolesConstant.USER + "]");
        } else {
            // 设置角色信息
            String roles = request.getRoles().toString();
            user.setRoles(roles);
        }

        // 加密密码
        String encryptPassword = SecurityUtils.encryptPassword(request.getPassword());
        user.setPassword(encryptPassword);

        // 保存用户信息
        boolean result = save(user);
        if (!result) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "添加用户失败");
        }

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
        User user = BeanCotyUtils.copyProperties(request, User.class);

        if (request.getPassword() != null) {
            String password = request.getPassword();
            String encryptPassword = SecurityUtils.encryptPassword(password);
            user.setPassword(encryptPassword);
        }
        return updateById(user);
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
    public PageResult<UserWalletFlowInfoVo> getUserWalletFlow(Long userId, PageRequest request) {
        Page<UserWalletLog> userWalletFlow = userWalletLogService.getUserWalletFlow(userId, request);
        List<UserWalletFlowInfoVo> userWalletFlowInfoVos = new ArrayList<>();

        AtomicLong atomicLong = new AtomicLong(1);
        userWalletFlow.getRecords().forEach(userWalletLog -> {
            UserWalletFlowInfoVo walletFlowInfoVo = UserWalletFlowInfoVo.builder()
                    .index(atomicLong.getAndIncrement())
                    .afterBalance(userWalletLog.getAfterBalance())
                    .amount(userWalletLog.getAmount())
                    .beforeBalance(userWalletLog.getBeforeBalance())
                    .changeTime(userWalletLog.getCreatedAt())
                    .changeType(userWalletLog.getBizType())
                    .build();
            userWalletFlowInfoVos.add(walletFlowInfoVo);
        });
        return new PageResult<>(userWalletFlow.getTotal(), userWalletFlow.getPages(), userWalletFlow.getSize(), userWalletFlowInfoVos);

    }

    /**
     * 获取用户消费信息
     *
     * @param userId  用户id
     * @param request 查询参数
     * @return 用户消费信息
     */
    @Override
    public PageResult<UserConsumeInfo> getConsumeInfo(Long userId, PageRequest request) {
        Page<MallOrder> mallOrderPage = mallOrderService.getOrderPageByUserId(userId, request);
        AtomicLong atomicLong = new AtomicLong(1);
        List<UserConsumeInfo> userConsumeInfos = mallOrderPage.getRecords().stream()
                .map(order -> UserConsumeInfo.builder()
                        .index(atomicLong.getAndIncrement())
                        .orderNo(order.getOrderNo())
                        .payPrice(order.getPayAmount())
                        .finishTime(order.getFinishTime())
                        .totalPrice(order.getTotalAmount())
                        .userId(order.getUserId())
                        .build())
                .toList();
        return new PageResult<>(mallOrderPage.getCurrent(), mallOrderPage.getSize(), mallOrderPage.getTotal(), userConsumeInfos);
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
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "用户不存在");
        }

        return switch (request.getOperationType()) {
            case 1 -> userWalletService.rechargeWallet(request.getUserId(), request.getAmount(), request.getReason());
            case 2 -> userWalletService.deductBalance(request.getUserId(), request.getAmount(), request.getReason());
            default ->
                    throw new ServiceException(ResponseResultCode.PARAM_ERROR, "不支持的操作类型: " + request.getOperationType());
        };
    }

    @Override
    public User getUserById(Long userId) {
        return getById(userId);
    }

    @Override
    public UserWalletVo getUserWallet(Long userId) {
        UserWallet wallet = userWalletService.getUserWalletByUserId(userId);
        return BeanCotyUtils.copyProperties(wallet, UserWalletVo.class);
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

}
