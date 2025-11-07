package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.UserWalletLogMapper;
import cn.zhangchuangla.medicine.admin.service.UserWalletLogService;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Chuang
 */
@Service
public class UserWalletLogServiceImpl extends ServiceImpl<UserWalletLogMapper, UserWalletLog>
        implements UserWalletLogService {

    private final UserWalletLogMapper userWalletLogMapper;

    public UserWalletLogServiceImpl(UserWalletLogMapper userWalletLogMapper) {
        this.userWalletLogMapper = userWalletLogMapper;
    }

    @Override
    public Page<UserWalletLog> getUserWalletFlow(Long userId, PageRequest request) {
        Page<UserWalletLog> page = request.toPage();
        return userWalletLogMapper.getBillPageByUserId(userId, page);
    }
}




