package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.model.dto.AfterSaleDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallAfterSaleListDto;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 智能体售后服务接口。
 */
public interface MallAfterSaleService {

    /**
     * 分页查询售后列表。
     * <p>
     * 功能描述：查询管理端智能体可见的售后列表分页数据。
     *
     * @param request 查询参数，包含分页信息与筛选条件
     * @return 返回售后分页数据，记录元素类型为 {@link MallAfterSaleListDto}
     * @throws RuntimeException 异常说明：当 RPC 调用失败或参数校验异常时抛出运行时异常
     */
    Page<MallAfterSaleListDto> listAfterSales(MallAfterSaleListRequest request);

    /**
     * 根据售后申请 ID 查询售后详情。
     *
     * @param afterSaleId 售后申请 ID
     * @return 售后详情 DTO
     */
    AfterSaleDetailDto getAfterSaleDetail(Long afterSaleId);
}
