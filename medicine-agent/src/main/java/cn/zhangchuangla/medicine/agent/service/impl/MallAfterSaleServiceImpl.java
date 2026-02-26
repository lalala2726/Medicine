package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentAfterSaleRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent 售后服务 Dubbo Consumer 实现。
 */
@Service
public class MallAfterSaleServiceImpl implements MallAfterSaleService {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 10000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentAfterSaleRpcService adminAgentAfterSaleRpcService;

    @Override
    public Page<AfterSaleListVo> listAfterSales(MallAfterSaleListRequest request) {
        MallAfterSaleListRequest safeRequest = request == null ? new MallAfterSaleListRequest() : request;
        PageResult<AfterSaleListVo> result = adminAgentAfterSaleRpcService.listAfterSales(safeRequest);
        return toPage(result);
    }

    @Override
    public AfterSaleDetailVo getAfterSaleDetail(Long afterSaleId) {
        return adminAgentAfterSaleRpcService.getAfterSaleDetailById(afterSaleId);
    }

    private Page<AfterSaleListVo> toPage(PageResult<AfterSaleListVo> result) {
        if (result == null) {
            return new Page<>(1, 10, 0);
        }
        long pageNum = result.getPageNum() == null ? 1L : result.getPageNum();
        long pageSize = result.getPageSize() == null ? 10L : result.getPageSize();
        long total = result.getTotal() == null ? 0L : result.getTotal();

        Page<AfterSaleListVo> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(result.getRows() == null ? List.of() : result.getRows());
        return page;
    }
}
