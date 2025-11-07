package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserWalletLogMapper;
import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.service.UserWalletLogService;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.UUIDUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.UserWallet;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * @author Chuang
 */
@Service
public class UserWalletLogServiceImpl extends ServiceImpl<UserWalletLogMapper, UserWalletLog>
        implements UserWalletLogService, BaseService {

    private final UserWalletLogMapper userWalletLogMapper;

    public UserWalletLogServiceImpl(UserWalletLogMapper userWalletLogMapper) {
        this.userWalletLogMapper = userWalletLogMapper;
    }


    @Override
    public Page<UserWalletLog> getBillPageByUserId(Long userId, UserWalletBillRequest request, Page<UserWalletLog> walletLogPage) {
        Assert.notNull(userId, "用户ID不能为空");
        return userWalletLogMapper.getBillPageByUserId(userId, request, walletLogPage);
    }

    @Override
    public void recordWalletLog(UserWallet wallet, BigDecimal amount, BigDecimal beforeBalance, BigDecimal afterBalance,
                                String bizType, Integer changeType, String remark, String bizId) {
        Assert.notNull(wallet, "钱包不能为空");
        Assert.notNull(wallet.getId(), "钱包ID不能为空");
        Assert.notNull(wallet.getUserId(), "钱包用户不能为空");
        Assert.notNull(amount, "变动金额不能为空");
        Assert.notNull(bizType, "业务类型不能为空");
        Assert.notNull(changeType, "变动类型不能为空");

        UserWalletLog walletLog = UserWalletLog.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .flowNo(UUIDUtils.complex())
                .bizType(bizType)
                .bizId(bizId)
                .changeType(changeType)
                .amount(amount)
                .beforeBalance(Optional.ofNullable(beforeBalance).orElse(wallet.getBalance()))
                .afterBalance(Optional.ofNullable(afterBalance).orElse(wallet.getBalance()))
                .remark(remark)
                .build();
        save(walletLog);
    }
}




