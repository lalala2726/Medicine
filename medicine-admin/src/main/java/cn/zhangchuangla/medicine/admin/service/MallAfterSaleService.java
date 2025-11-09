package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.AfterSaleAuditRequest;
import cn.zhangchuangla.medicine.admin.model.request.AfterSaleListRequest;
import cn.zhangchuangla.medicine.admin.model.request.AfterSaleProcessRequest;
import cn.zhangchuangla.medicine.model.entity.MallAfterSale;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 售后申请Service(管理端)
 *
 * @author Chuang
 * created 2025/11/08
 */
public interface MallAfterSaleService extends IService<MallAfterSale> {

    /**
     * 查询售后列表(管理端)
     *
     * @param request 查询条件
     * @return 售后列表
     */
    Page<AfterSaleListVo> getAfterSaleList(AfterSaleListRequest request);

    /**
     * 查询售后详情(管理端)
     *
     * @param afterSaleId 售后申请ID
     * @return 售后详情
     */
    AfterSaleDetailVo getAfterSaleDetail(Long afterSaleId);

    /**
     * 审核售后申请
     *
     * @param request 审核请求
     * @return 是否审核成功
     */
    boolean auditAfterSale(AfterSaleAuditRequest request);

    /**
     * 处理售后退款
     *
     * @param request 处理请求
     * @return 是否处理成功
     */
    boolean processRefund(AfterSaleProcessRequest request);

    /**
     * 处理换货
     *
     * @param request 处理请求
     * @return 是否处理成功
     */
    boolean processExchange(AfterSaleProcessRequest request);
}

