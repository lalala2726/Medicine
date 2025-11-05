package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
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
     * @param userId        用户ID
     * @param request       查询参数
     * @param walletLogPage 分页参数
     * @return 用户钱包流水
     */
    Page<UserWalletLog> getBillPageByUserId(Long userId, UserWalletBillRequest request, Page<UserWalletLog> walletLogPage);
}
