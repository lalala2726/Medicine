package cn.zhangchuangla.medicine.rpc.admin;

import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;

/**
 * 管理端 Agent 售后只读 RPC。
 */
public interface AdminAgentAfterSaleRpcService {

    /**
     * 分页查询售后列表。
     *
     * @param query 售后查询参数
     * @return 售后分页结果
     */
    PageResult<AfterSaleListVo> listAfterSales(MallAfterSaleListRequest query);

    /**
     * 根据售后申请 ID 查询售后详情。
     *
     * @param afterSaleId 售后申请 ID
     * @return 售后详情
     */
    AfterSaleDetailVo getAfterSaleDetailById(Long afterSaleId);
}
