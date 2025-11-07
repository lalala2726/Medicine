package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.client.model.vo.UserWalletBillVo;
import cn.zhangchuangla.medicine.model.entity.UserWallet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Chuang
 */
public interface UserWalletService extends IService<UserWallet> {

    /**
     * 获取用户钱包余额
     *
     * @return 钱包余额
     */
    BigDecimal getUserWalletBalance();

    /**
     * 获取用户钱包流水
     *
     * @param request 查询参数
     * @return 流水列表
     */
    List<UserWalletBillVo> getBillList(UserWalletBillRequest request);

    /**
     * 扣除用户钱包余额
     *
     * @param userId  用户ID
     * @param amount  扣除金额
     * @param bizType 业务类型
     * @return 是否扣除成功
     */
    boolean deductBalance(Long userId, BigDecimal amount, String bizType);


    /**
     * 开通钱包
     *
     * @param userId 用户ID
     * @return 是否开通成功
     */
    boolean openWallet(Long userId);

    /**
     * 充值钱包
     *
     * @param userId 用户ID
     * @param amount 充值金额
     * @param reason 充值原因
     * @return 是否充值成功
     */
    boolean rechargeWallet(Long userId, BigDecimal amount, String reason);

    /**
     * 冻结钱包
     *
     * @param userId 用户ID
     * @param reason 冻结原因
     * @return 是否冻结成功
     */
    boolean freezeWallet(Long userId, String reason);

    /**
     * 解冻钱包
     *
     * @param userId 用户ID
     * @param reason 解冻原因
     * @return 是否解冻成功
     */
    boolean unfreezeWallet(Long userId, String reason);


}
