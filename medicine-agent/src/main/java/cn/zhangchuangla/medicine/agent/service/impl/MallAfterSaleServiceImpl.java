package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.model.dto.AfterSaleDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallAfterSaleListDto;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentAfterSaleRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * Agent 售后服务 Dubbo Consumer 实现。
 */
@Service
public class MallAfterSaleServiceImpl implements MallAfterSaleService {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 10000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentAfterSaleRpcService adminAgentAfterSaleRpcService;

    /**
     * 功能描述：通过 Dubbo 调用管理端服务，查询售后分页数据。
     *
     * @param request 售后分页查询参数，包含分页信息与筛选条件
     * @return 返回售后分页结果，记录类型为 {@link MallAfterSaleListDto}
     * @throws RuntimeException 异常说明：当 Dubbo 远程调用失败时抛出运行时异常
     */
    @Override
    public Page<MallAfterSaleListDto> listAfterSales(MallAfterSaleListRequest request) {
        return adminAgentAfterSaleRpcService.listAfterSales(request);
    }

    /**
     * 功能描述：通过 Dubbo 调用管理端服务查询售后详情。
     *
     * @param afterSaleId 售后申请 ID
     * @return 返回售后详情 DTO 对象
     * @throws RuntimeException 异常说明：当 Dubbo 远程调用失败时抛出运行时异常
     */
    @Override
    public AfterSaleDetailDto getAfterSaleDetail(Long afterSaleId) {
        return adminAgentAfterSaleRpcService.getAfterSaleDetailById(afterSaleId);
    }
}
