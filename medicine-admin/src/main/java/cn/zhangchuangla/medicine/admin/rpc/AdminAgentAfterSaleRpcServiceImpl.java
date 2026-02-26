package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.model.request.AfterSaleListRequest;
import cn.zhangchuangla.medicine.admin.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentAfterSaleRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 管理端 Agent 售后 RPC Provider。
 */
@DubboService(interfaceClass = AdminAgentAfterSaleRpcService.class, group = "medicine-admin", version = "1.0.0")
@RequiredArgsConstructor
public class AdminAgentAfterSaleRpcServiceImpl implements AdminAgentAfterSaleRpcService {

    private final MallAfterSaleService mallAfterSaleService;

    @Override
    public PageResult<AfterSaleListVo> listAfterSales(MallAfterSaleListRequest query) {
        MallAfterSaleListRequest safeQuery = query == null ? new MallAfterSaleListRequest() : query;
        AfterSaleListRequest request = BeanCotyUtils.copyProperties(safeQuery, AfterSaleListRequest.class);
        Page<AfterSaleListVo> page = mallAfterSaleService.getAfterSaleList(request);
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public AfterSaleDetailVo getAfterSaleDetailById(Long afterSaleId) {
        return mallAfterSaleService.getAfterSaleDetail(afterSaleId);
    }
}
