package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserWalletMapper;
import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.model.vo.UserWalletBillVo;
import cn.zhangchuangla.medicine.client.service.UserWalletLogService;
import cn.zhangchuangla.medicine.client.service.UserWalletService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.UserWallet;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Chuang
 */
@Service
public class UserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet>
        implements UserWalletService, BaseService {

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
        Page<UserWalletLog> walletLogPage = new Page<>(request.getPageNum(), request.getPageSize());
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
}




