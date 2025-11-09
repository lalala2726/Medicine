package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.UserWalletMapper;
import cn.zhangchuangla.medicine.admin.service.UserWalletLogService;
import cn.zhangchuangla.medicine.admin.service.UserWalletService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.UUIDUtils;
import cn.zhangchuangla.medicine.model.dto.UserWalletLogRecordDto;
import cn.zhangchuangla.medicine.model.entity.UserWallet;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chuang
 */
@Service
public class UserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet>
        implements UserWalletService {

    private static final int WALLET_STATUS_NORMAL = 0;
    private static final int WALLET_STATUS_FROZEN = 1;

    private final UserWalletLogService userWalletLogService;

    public UserWalletServiceImpl(UserWalletLogService userWalletLogService) {
        this.userWalletLogService = userWalletLogService;
    }

    @Override
    public boolean openWallet(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        boolean exists = lambdaQuery()
                .eq(UserWallet::getUserId, userId)
                .count() > 0;
        if (exists) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "用户已开通钱包");
        }
        UserWallet userWallet = UserWallet.builder()
                .userId(userId)
                .walletNo(UUIDUtils.complex())
                .balance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .totalIncome(BigDecimal.ZERO)
                .totalExpend(BigDecimal.ZERO)
                .currency("CNY")
                .status(WALLET_STATUS_NORMAL)
                .remark("用户钱包开通成功")
                .build();
        return save(userWallet);
    }

    @Override
    public boolean rechargeWallet(Long userId, BigDecimal amount, String reason) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(amount, "充值金额不能为空");
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

        if (!updateById(userWallet)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "充值失败, 请稍后重试");
        }
        recordWalletLog(userWallet, amount, beforeBalance, newBalance, reason, 1, reason);
        return true;
    }

    @Override
    public boolean deductBalance(Long userId, BigDecimal amount, String reason) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(amount, "扣减金额不能为空");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "扣减金额必须大于0");
        Assert.notEmpty(reason, "扣减原因不能为空");

        UserWallet userWallet = getWalletOrThrow(userId);
        ensureWalletNotFrozen(userWallet);

        BigDecimal beforeBalance = safeAmount(userWallet.getBalance());
        if (beforeBalance.compareTo(amount) < 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包余额不足");
        }
        BigDecimal afterBalance = beforeBalance.subtract(amount);

        userWallet.setBalance(afterBalance);
        userWallet.setTotalExpend(safeAmount(userWallet.getTotalExpend()).add(amount));
        userWallet.setRemark(reason);

        if (!updateById(userWallet)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "扣款失败, 请稍后重试");
        }
        recordWalletLog(userWallet, amount, beforeBalance, afterBalance, reason, 2, reason);
        return true;
    }

    @Override
    public boolean freezeWallet(Long userId, String reason) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notEmpty(reason, "冻结原因不能为空");

        UserWallet userWallet = getWalletOrThrow(userId);
        if (isWalletFrozen(userWallet)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包已冻结");
        }

        userWallet.setStatus(WALLET_STATUS_FROZEN);
        userWallet.setFreezeReason(reason);
        userWallet.setFreezeTime(new Date());
        userWallet.setRemark(reason);

        if (!updateById(userWallet)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "冻结失败, 请稍后重试");
        }
        BigDecimal balance = safeAmount(userWallet.getBalance());
        recordWalletLog(userWallet, BigDecimal.ZERO, balance, balance, reason, 3, reason);
        return true;
    }

    @Override
    public boolean unfreezeWallet(Long userId, String reason) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notEmpty(reason, "解冻原因不能为空");

        UserWallet userWallet = getWalletOrThrow(userId);
        if (!isWalletFrozen(userWallet)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包未冻结");
        }

        userWallet.setStatus(WALLET_STATUS_NORMAL);
        userWallet.setFreezeReason(null);
        userWallet.setFreezeTime(null);
        userWallet.setRemark(reason);

        if (!updateById(userWallet)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "解冻失败, 请稍后重试");
        }
        BigDecimal balance = safeAmount(userWallet.getBalance());
        recordWalletLog(userWallet, BigDecimal.ZERO, balance, balance, reason, 4, reason);
        return true;
    }

    @Override
    public UserWallet getUserWalletByUserId(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        UserWallet userWallet = lambdaQuery()
                .eq(UserWallet::getUserId, userId)
                .one();
        if (userWallet == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "用户钱包不存在");
        }
        return userWallet;
    }

    private UserWallet getWalletOrThrow(Long userId) {
        UserWallet userWallet = lambdaQuery()
                .eq(UserWallet::getUserId, userId)
                .one();
        if (userWallet == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "用户钱包不存在");
        }
        return userWallet;
    }

    private void ensureWalletNotFrozen(UserWallet userWallet) {
        if (isWalletFrozen(userWallet)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包已冻结, 暂不可操作");
        }
    }

    private boolean isWalletFrozen(UserWallet userWallet) {
        return Integer.valueOf(WALLET_STATUS_FROZEN).equals(userWallet.getStatus());
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void recordWalletLog(UserWallet wallet, BigDecimal amount, BigDecimal beforeBalance,
                                 BigDecimal afterBalance, String reason, Integer changeType, String remark) {
        UserWalletLogRecordDto recordDto = UserWalletLogRecordDto.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .reason(reason)
                .changeType(changeType)
                .remark(remark)
                .build();
        userWalletLogService.recordWalletLog(recordDto);
    }
}
