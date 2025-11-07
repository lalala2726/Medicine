package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserWalletMapper;
import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.model.vo.UserWalletBillVo;
import cn.zhangchuangla.medicine.client.service.UserWalletLogService;
import cn.zhangchuangla.medicine.client.service.UserWalletService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.UUIDUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.UserWallet;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Chuang
 */
@Service
public class UserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet>
        implements UserWalletService, BaseService {

    private static final int WALLET_STATUS_NORMAL = 0;
    private static final int WALLET_STATUS_FROZEN = 1;
    private static final String BIZ_TYPE_RECHARGE = "recharge";
    private static final String BIZ_TYPE_FREEZE = "freeze";
    private static final String BIZ_TYPE_UNFREEZE = "unfreeze";

    private final UserWalletLogService userWalletLogService;

    public UserWalletServiceImpl(UserWalletLogService userWalletLogService) {
        this.userWalletLogService = userWalletLogService;
    }


    @Override
    public BigDecimal getUserWalletBalance() {
        Long userId = getUserId();
        UserWallet userWallet = lambdaQuery()
                .eq(UserWallet::getUserId, userId)
                .one();
        if (userWallet == null) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "您的钱包还未开通, 请先开通钱包");
        }
        return userWallet.getBalance();
    }

    @Override
    public List<UserWalletBillVo> getBillList(UserWalletBillRequest request) {
        Page<UserWalletLog> walletLogPage = request.toPage();
        Long userId = getUserId();
        Page<UserWalletLog> page = userWalletLogService.getBillPageByUserId(userId, request, walletLogPage);
        AtomicLong counter = new AtomicLong(1);
        return page.getRecords().stream()
                .map(bill -> UserWalletBillVo.builder()
                        .index(counter.getAndIncrement())
                        .title(bill.getBizType())
                        .time(bill.getCreatedAt())
                        .amount(bill.getAmount())
                        .build())
                .toList();

    }

    @Override
    public boolean deductBalance(Long userId, BigDecimal amount, String bizType) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(amount, "扣减金额不能为空");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "扣减金额必须大于0");
        Assert.notEmpty(bizType, "业务类型不能为空");

        UserWallet userWallet = getWalletOrThrow(userId);
        ensureWalletNotFrozen(userWallet);

        BigDecimal beforeBalance = safeAmount(userWallet.getBalance());
        if (beforeBalance.compareTo(amount) < 0) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "钱包余额不足");
        }
        BigDecimal afterBalance = beforeBalance.subtract(amount);
        userWallet.setBalance(afterBalance);
        userWallet.setTotalExpend(safeAmount(userWallet.getTotalExpend()).add(amount));
        userWallet.setRemark("扣减-" + bizType);

        boolean updated = updateById(userWallet);
        if (!updated) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "扣减失败, 请稍后重试");
        }
        userWalletLogService.recordWalletLog(userWallet, amount, beforeBalance, afterBalance, bizType, 2,
                "余额扣减-" + bizType, null);
        return true;
    }

    @Override
    public boolean openWallet(Long userId) {
        // 检查用户是否已经开通钱包
        boolean isOpen = lambdaQuery()
                .eq(UserWallet::getUserId, userId)
                .count() > 0;
        if (isOpen) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "用户已开通钱包");
        }
        // 生成钱包的唯一ID
        String uuid = UUIDUtils.complex();
        UserWallet userWallet = UserWallet.builder()
                .userId(userId)
                .walletNo(uuid)
                .balance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .totalIncome(BigDecimal.ZERO)
                .totalExpend(BigDecimal.ZERO)
                .status(WALLET_STATUS_NORMAL)
                .currency("CNY")
                .remark("用户钱包开通成功")
                .build();
        return save(userWallet);
    }

    @Override
    public boolean rechargeWallet(Long userId, BigDecimal amount, String reason) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "充值金额必须大于0");
        Assert.notEmpty(reason, "充值原因不能为空");

        UserWallet userWallet = getWalletOrThrow(userId);
        ensureWalletNotFrozen(userWallet);

        BigDecimal beforeBalance = safeAmount(userWallet.getBalance());
        BigDecimal newBalance = beforeBalance.add(amount);
        BigDecimal newTotalIncome = safeAmount(userWallet.getTotalIncome()).add(amount);

        userWallet.setBalance(newBalance);
        userWallet.setTotalIncome(newTotalIncome);
        userWallet.setRemark(reason);

        boolean updated = updateById(userWallet);
        if (!updated) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "充值失败, 请稍后重试");
        }
        userWalletLogService.recordWalletLog(userWallet, amount, beforeBalance, newBalance, BIZ_TYPE_RECHARGE, 1,
                reason, null);
        return true;
    }

    @Override
    public boolean freezeWallet(Long userId, String reason) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notEmpty(reason, "冻结原因不能为空");

        UserWallet userWallet = getWalletOrThrow(userId);
        if (isWalletFrozen(userWallet)) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "钱包已冻结");
        }

        userWallet.setStatus(WALLET_STATUS_FROZEN);
        userWallet.setFreezeReason(reason);
        userWallet.setFreezeTime(new Date());
        userWallet.setRemark(reason);

        boolean updated = updateById(userWallet);
        if (!updated) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "冻结失败, 请稍后重试");
        }
        BigDecimal balance = safeAmount(userWallet.getBalance());
        userWalletLogService.recordWalletLog(userWallet, BigDecimal.ZERO, balance, balance, BIZ_TYPE_FREEZE, 3,
                reason, null);
        return true;
    }

    @Override
    public boolean unfreezeWallet(Long userId, String reason) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notEmpty(reason, "解冻原因不能为空");

        UserWallet userWallet = getWalletOrThrow(userId);
        if (!isWalletFrozen(userWallet)) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "钱包未冻结");
        }

        userWallet.setStatus(WALLET_STATUS_NORMAL);
        userWallet.setFreezeReason(null);
        userWallet.setFreezeTime(null);
        userWallet.setRemark(reason);

        boolean updated = updateById(userWallet);
        if (!updated) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "解冻失败, 请稍后重试");
        }
        BigDecimal balance = safeAmount(userWallet.getBalance());
        userWalletLogService.recordWalletLog(userWallet, BigDecimal.ZERO, balance, balance, BIZ_TYPE_UNFREEZE, 4,
                reason, null);
        return true;
    }

    private UserWallet getWalletOrThrow(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        UserWallet userWallet = lambdaQuery()
                .eq(UserWallet::getUserId, userId)
                .one();
        if (userWallet == null) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "用户钱包不存在");
        }
        return userWallet;
    }

    private void ensureWalletNotFrozen(UserWallet userWallet) {
        if (isWalletFrozen(userWallet)) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "钱包已冻结, 暂不可操作");
        }
    }

    private boolean isWalletFrozen(UserWallet userWallet) {
        return Integer.valueOf(WALLET_STATUS_FROZEN).equals(userWallet.getStatus());
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}



