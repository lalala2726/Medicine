package cn.zhangchuangla.medicine.client.mapper;

import cn.zhangchuangla.medicine.client.model.request.UserWalletBillRequest;
import cn.zhangchuangla.medicine.model.entity.UserWalletLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface UserWalletLogMapper extends BaseMapper<UserWalletLog> {

    /**
     * 获取用户钱包流水
     *
     * @param userId        用户ID
     * @param request       筛选参数
     * @param walletLogPage 分页参数
     * @return 用户钱包流水
     */
    Page<UserWalletLog> getBillPageByUserId(@Param("userId") Long userId, @Param("request") UserWalletBillRequest request, Page<UserWalletLog> walletLogPage);

}




