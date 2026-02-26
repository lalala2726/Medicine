package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 智能体售后服务接口。
 */
public interface MallAfterSaleService {

    /**
     * 分页查询售后列表。
     *
     * @param request 查询参数
     * @return 售后分页数据
     */
    Page<AfterSaleListVo> listAfterSales(MallAfterSaleListRequest request);

    /**
     * 根据售后申请 ID 查询售后详情。
     *
     * @param afterSaleId 售后申请 ID
     * @return 售后详情
     */
    AfterSaleDetailVo getAfterSaleDetail(Long afterSaleId);
}
