package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserWalletLogMapper;
import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.service.UserWalletLogService;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.UUIDUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.dto.UserWalletLogRecordDto;
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
    public void recordWalletLog(UserWalletLogRecordDto recordDto) {
        Assert.notNull(recordDto, "钱包流水不能为空");
        Assert.notNull(recordDto.getWalletId(), "钱包ID不能为空");
        Assert.notNull(recordDto.getUserId(), "钱包用户不能为空");
        Assert.notNull(recordDto.getAmount(), "变动金额不能为空");
        Assert.notNull(recordDto.getBizType(), "业务类型不能为空");
        Assert.notNull(recordDto.getChangeType(), "变动类型不能为空");

        UserWalletLog walletLog = UserWalletLog.builder()
                .walletId(recordDto.getWalletId())
                .userId(recordDto.getUserId())
                .flowNo(Optional.ofNullable(recordDto.getFlowNo()).orElse(UUIDUtils.complex()))
                .bizType(recordDto.getBizType())
                .bizId(recordDto.getBizId())
                .changeType(recordDto.getChangeType())
                .amount(recordDto.getAmount())
                .beforeBalance(Optional.ofNullable(recordDto.getBeforeBalance()).orElse(BigDecimal.ZERO))
                .afterBalance(Optional.ofNullable(recordDto.getAfterBalance()).orElse(BigDecimal.ZERO))
                .remark(recordDto.getRemark())
                .build();
        save(walletLog);
    }
}




