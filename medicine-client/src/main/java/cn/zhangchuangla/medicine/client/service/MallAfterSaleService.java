package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.*;
import cn.zhangchuangla.medicine.model.entity.MallAfterSale;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 售后申请Service
 *
 * @author Chuang
 * created 2025/11/08
 */
public interface MallAfterSaleService extends IService<MallAfterSale> {

    /**
     * 用户申请售后
     *
     * @param request 申请售后请求
     * @return 售后单号
     */
    String applyAfterSale(AfterSaleApplyRequest request);

    /**
     * 用户取消售后申请
     *
     * @param request 取消售后请求
     * @return 是否取消成功
     */
    boolean cancelAfterSale(AfterSaleCancelRequest request);

    /**
     * 查询售后列表
     *
     * @param request 查询条件
     * @return 售后列表
     */
    Page<AfterSaleListVo> getAfterSaleList(AfterSaleListRequest request);

    /**
     * 查询售后详情
     *
     * @param afterSaleId 售后申请ID
     * @return 售后详情
     */
    AfterSaleDetailVo getAfterSaleDetail(Long afterSaleId);

    /**
     * 申请整单退款
     *
     * @param request 退款请求
     * @return 创建的售后单号列表
     */
    List<String> applyOrderRefund(OrderRefundApplyRequest request);

    /**
     * 再次发起售后
     *
     * @param request 重新申请请求
     * @return 售后单号
     */
    String reapplyAfterSale(AfterSaleReapplyRequest request);
}
