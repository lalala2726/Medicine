package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.model.entity.UserWallet;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

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

    /**
     * 记录钱包流水
     *
     * @param wallet        钱包
     * @param amount        变动金额
     * @param beforeBalance 变动前余额
     * @param afterBalance  变动后余额
     * @param bizType       业务类型
     * @param changeType    变动类型
     * @param remark        备注
     * @param bizId         业务关联ID
     */
    void recordWalletLog(UserWallet wallet, BigDecimal amount, BigDecimal beforeBalance, BigDecimal afterBalance,
                         String bizType, Integer changeType, String remark, String bizId);
}
