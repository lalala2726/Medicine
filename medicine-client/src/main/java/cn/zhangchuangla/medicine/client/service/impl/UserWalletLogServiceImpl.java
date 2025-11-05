package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.UserWalletLogMapper;
import cn.zhangchuangla.medicine.client.mapper.UserWalletMapper;
import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.service.UserWalletLogService;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Chuang
 */
@Service
public class UserWalletLogServiceImpl extends ServiceImpl<UserWalletLogMapper, UserWalletLog>
        implements UserWalletLogService, BaseService {

    private final UserWalletMapper userWalletMapper;

    public UserWalletLogServiceImpl(UserWalletMapper userWalletMapper) {
        this.userWalletMapper = userWalletMapper;
    }


    @Override
    public Page<UserWalletLog> getBillPageByUserId(Long userId, UserWalletBillRequest request, Page<UserWalletLog> walletLogPage) {
        Assert.notNull(userId, "用户ID不能为空");
        return null;
    }
}




