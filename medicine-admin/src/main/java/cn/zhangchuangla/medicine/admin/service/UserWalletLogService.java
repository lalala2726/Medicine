package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface UserWalletLogService extends IService<UserWalletLog> {

    /**
     * 获取用户钱包流水
     *
     * @param userId  用户ID
     * @param request 列表查询参数
     * @return 用户钱包流水
     */
    Page<UserWalletLog> getUserWalletFlow(Long userId, PageRequest request);
}
