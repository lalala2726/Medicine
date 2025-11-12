package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserWalletMapper;
import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.service.UserWalletLogService;
import cn.zhangchuangla.medicine.client.service.UserWalletService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.dto.UserWalletLogRecordDto;
import cn.zhangchuangla.medicine.model.entity.UserWallet;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Chuang
 */
@Service
public class UserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet>
        implements UserWalletService, BaseService {

    private static final int WALLET_STATUS_FROZEN = 1;

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
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "您的钱包还未开通, 请先开通钱包");
        }
        return userWallet.getBalance();
    }

    @Override
    public Page<UserWalletLog> getBillList(UserWalletBillRequest request) {
        Page<UserWalletLog> walletLogPage = request.toPage();
        Long userId = getUserId();
        return userWalletLogService.getBillPageByUserId(userId, request, walletLogPage);
    }

    @Override
    public boolean deductBalance(Long userId, BigDecimal amount, String reason) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(amount, "扣减金额不能为空");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "扣减金额必须大于0");
        Assert.notEmpty(reason, "原因不能为空");

        UserWallet userWallet = getWalletOrThrow(userId);
        ensureWalletNotFrozen(userWallet);

        BigDecimal beforeBalance = safeAmount(userWallet.getBalance());
        if (beforeBalance.compareTo(amount) < 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包余额不足");
        }
        BigDecimal afterBalance = beforeBalance.subtract(amount);
        userWallet.setBalance(afterBalance);
        userWallet.setTotalExpend(safeAmount(userWallet.getTotalExpend()).add(amount));
        userWallet.setRemark("扣减-" + reason);

        boolean updated = updateById(userWallet);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "扣减失败, 请稍后重试");
        }
        UserWalletLogRecordDto recordDto = UserWalletLogRecordDto.builder()
                .walletId(userWallet.getId())
                .userId(userWallet.getUserId())
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .reason(reason)
                .changeType(2)
                .remark("余额扣减-" + reason)
                .build();
        userWalletLogService.recordWalletLog(recordDto);
        return true;
    }


    private UserWallet getWalletOrThrow(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
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
}
