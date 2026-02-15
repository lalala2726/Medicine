package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserMapper;
import cn.zhangchuangla.medicine.client.model.dto.UserProfileDto;
import cn.zhangchuangla.medicine.client.model.vo.UserBriefVo;
import cn.zhangchuangla.medicine.client.service.MallOrderService;
import cn.zhangchuangla.medicine.client.service.UserService;
import cn.zhangchuangla.medicine.client.service.UserWalletService;
import cn.zhangchuangla.medicine.common.core.entity.IPEntity;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.core.utils.IPUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.enums.OrderStatusEnum;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chuang
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService, BaseService {

    private final UserWalletService userWalletService;
    private final MallOrderService mallOrderService;

    public UserServiceImpl(UserWalletService userWalletService, MallOrderService mallOrderService) {
        this.userWalletService = userWalletService;
        this.mallOrderService = mallOrderService;
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
        List<String> roleCodes = baseMapper.listRoleCodesByUserId(userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Set.of();
        }
        return roleCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toSet());
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
        return getUserRolesByUserId(user.getId());
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

    @Override
    public UserBriefVo getUserBriefInfo() {
        Long userId = getUserId();

        // 获取当前用户信息
        User user = getUserById(userId);

        // 获取用户钱包余额
        BigDecimal userWalletBalance = userWalletService.getUserWalletBalance();

        // 获取当前用户订单的信息
        List<MallOrder> mallOrders = mallOrderService.lambdaQuery()
                .eq(MallOrder::getUserId, userId)
                .list();

        // 统计各种状态的订单数量
        Map<String, Long> orderCountMap = mallOrders.stream()
                .collect(Collectors.groupingBy(MallOrder::getOrderStatus, Collectors.counting()));

        // 待支付订单数量
        Integer payOrderCount = orderCountMap.getOrDefault(OrderStatusEnum.PENDING_PAYMENT.getType(), 0L).intValue();

        // 待发货订单数量
        Integer deliverOrderCount = orderCountMap.getOrDefault(OrderStatusEnum.PENDING_SHIPMENT.getType(), 0L).intValue();

        // 待收货订单数量
        Integer receiveOrderCount = orderCountMap.getOrDefault(OrderStatusEnum.PENDING_RECEIPT.getType(), 0L).intValue();

        // 已完成订单数量
        Integer completeOrderCount = orderCountMap.getOrDefault(OrderStatusEnum.COMPLETED.getType(), 0L).intValue();

        // 退货/售后订单数量(售后中)
        Integer afterSaleOrderCount = orderCountMap.getOrDefault(OrderStatusEnum.AFTER_SALE.getType(), 0L).intValue();

        // TODO: 优惠券数量需要从优惠券服务获取，暂时设置为0
        Integer couponCount = 0;

        return UserBriefVo.builder()
                .avatarUrl(user.getAvatar())
                .nickName(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .balance(userWalletBalance)
                .couponCount(couponCount)
                .payOrderCount(payOrderCount)
                .deliverOrderCount(deliverOrderCount)
                .receiveOrderCount(receiveOrderCount)
                .completeOrderCount(completeOrderCount)
                .afterSaleOrderCount(afterSaleOrderCount)
                .build();
    }

    @Override
    public UserProfileDto getUserProfile() {
        Long userId = getUserId();
        User user = getUserById(userId);
        return BeanCotyUtils.copyProperties(user, UserProfileDto.class);
    }

    @Override
    public boolean updateUserProfile(UserProfileDto userProfileDto) {
        Long userId = getUserId();
        User user = BeanCotyUtils.copyProperties(userProfileDto, User.class);
        user.setId(userId);
        return updateById(user);
    }

}



