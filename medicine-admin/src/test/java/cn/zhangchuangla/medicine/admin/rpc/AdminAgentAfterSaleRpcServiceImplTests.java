package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.model.request.AfterSaleListRequest;
import cn.zhangchuangla.medicine.admin.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.model.dto.MallAfterSaleListDto;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAgentAfterSaleRpcServiceImplTests {

    @Mock
    private MallAfterSaleService mallAfterSaleService;

    @InjectMocks
    private AdminAgentAfterSaleRpcServiceImpl rpcService;

    /**
     * 测试目的：验证 RPC 列表查询会归一化分页参数与筛选条件（支持 value 包裹结构和中文名称）。
     * 预期结果：传入服务层的请求参数中 pageNum/pageSize 使用默认值，afterSaleType/afterSaleStatus/applyReason 被转换为标准码值。
     */
    @Test
    void listAfterSales_ShouldNormalizeQueryValues() {
        MallAfterSaleListRequest query = new MallAfterSaleListRequest();
        query.setPageNum(0);
        query.setPageSize(0);
        query.setAfterSaleType("{\"value\":\"REFUND_ONLY\",\"description\":\"仅退款\"}");
        query.setAfterSaleStatus("{value=PENDING, description=待审核}");
        query.setApplyReason("收到商品损坏了");
        query.setOrderNo("  O20251108001  ");
        query.setUserId(1001L);

        when(mallAfterSaleService.getAfterSaleList(any(AfterSaleListRequest.class)))
                .thenReturn(new Page<MallAfterSaleListDto>(1, 10, 0));

        rpcService.listAfterSales(query);

        ArgumentCaptor<AfterSaleListRequest> captor = ArgumentCaptor.forClass(AfterSaleListRequest.class);
        verify(mallAfterSaleService).getAfterSaleList(captor.capture());
        AfterSaleListRequest actual = captor.getValue();

        assertEquals(1, actual.getPageNum());
        assertEquals(10, actual.getPageSize());
        assertEquals("REFUND_ONLY", actual.getAfterSaleType());
        assertEquals("PENDING", actual.getAfterSaleStatus());
        assertEquals("DAMAGED", actual.getApplyReason());
        assertEquals("O20251108001", actual.getOrderNo());
        assertEquals(1001L, actual.getUserId());
    }

    /**
     * 测试目的：验证当查询参数为 null 时，RPC 层会构造默认请求并继续调用服务层。
     * 预期结果：传入服务层的请求对象不为空，且分页参数保持默认值。
     */
    @Test
    void listAfterSales_WithNullQuery_ShouldUseDefaultRequest() {
        when(mallAfterSaleService.getAfterSaleList(any(AfterSaleListRequest.class)))
                .thenReturn(new Page<MallAfterSaleListDto>(1, 10, 0));

        rpcService.listAfterSales(null);

        ArgumentCaptor<AfterSaleListRequest> captor = ArgumentCaptor.forClass(AfterSaleListRequest.class);
        verify(mallAfterSaleService).getAfterSaleList(captor.capture());
        AfterSaleListRequest actual = captor.getValue();

        assertEquals(1, actual.getPageNum());
        assertEquals(10, actual.getPageSize());
    }
}
