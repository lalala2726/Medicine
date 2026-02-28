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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAgentAfterSaleRpcServiceImplTests {

    @Mock
    private MallAfterSaleService mallAfterSaleService;

    @InjectMocks
    private AdminAgentAfterSaleRpcServiceImpl rpcService;

    /**
     * 测试目的：验证 RPC 列表查询仅做对象属性复制，不对分页和筛选字段做额外归一化处理。
     * 预期结果：传入服务层的请求参数与入参保持一致，包括 pageNum/pageSize、编码包装字符串与空白字符。
     */
    @Test
    void listAfterSales_ShouldCopyRawQueryValues() {
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

        assertEquals(0, actual.getPageNum());
        assertEquals(0, actual.getPageSize());
        assertEquals("{\"value\":\"REFUND_ONLY\",\"description\":\"仅退款\"}", actual.getAfterSaleType());
        assertEquals("{value=PENDING, description=待审核}", actual.getAfterSaleStatus());
        assertEquals("收到商品损坏了", actual.getApplyReason());
        assertEquals("  O20251108001  ", actual.getOrderNo());
        assertEquals(1001L, actual.getUserId());
    }

    /**
     * 测试目的：验证当查询参数为 null 时，RPC 层会将 null 直接传递给服务层。
     * 预期结果：服务层 getAfterSaleList 方法收到 null 参数。
     */
    @Test
    void listAfterSales_WithNullQuery_ShouldPassNullToService() {
        rpcService.listAfterSales(null);
        verify(mallAfterSaleService).getAfterSaleList(null);
        verifyNoMoreInteractions(mallAfterSaleService);
    }
}
