package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgentAfterSaleControllerTests {

    private final StubMallAfterSaleService mallAfterSaleService = new StubMallAfterSaleService();
    private final AgentAfterSaleController controller = new AgentAfterSaleController(mallAfterSaleService);

    @Test
    void listAfterSales_ShouldDelegateToService() {
        MallAfterSaleListRequest request = new MallAfterSaleListRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        mallAfterSaleService.page = createSamplePage();

        var result = controller.listAfterSales(request);

        assertEquals(200, result.getCode());
        assertTrue(mallAfterSaleService.listAfterSalesInvoked);
        assertEquals(request, mallAfterSaleService.capturedRequest);
        assertNotNull(result.getData());
    }

    @Test
    void listAfterSales_WithNullRequest_ShouldUseDefault() {
        mallAfterSaleService.page = createSamplePage();

        var result = controller.listAfterSales(null);

        assertEquals(200, result.getCode());
        assertTrue(mallAfterSaleService.listAfterSalesInvoked);
        assertNotNull(mallAfterSaleService.capturedRequest);
    }

    @Test
    void getAfterSaleDetail_ShouldDelegateToService() {
        Long afterSaleId = 1L;
        mallAfterSaleService.detail = createSampleDetail();

        var result = controller.getAfterSaleDetail(afterSaleId);

        assertEquals(200, result.getCode());
        assertTrue(mallAfterSaleService.getAfterSaleDetailInvoked);
        assertEquals(afterSaleId, mallAfterSaleService.capturedAfterSaleId);
        assertNotNull(result.getData());
        assertEquals("AS20251108001", result.getData().getAfterSaleNo());
    }

    private Page<AfterSaleListVo> createSamplePage() {
        Page<AfterSaleListVo> page = new Page<>(1, 10);
        page.setTotal(1);

        AfterSaleListVo item = new AfterSaleListVo();
        item.setId(1L);
        item.setAfterSaleNo("AS20251108001");
        item.setOrderNo("O20251108001");
        item.setUserId(1001L);
        item.setUserNickname("张三");
        item.setProductName("感冒药");
        item.setAfterSaleType("REFUND_ONLY");
        item.setAfterSaleTypeName("仅退款");
        item.setAfterSaleStatus("PENDING");
        item.setAfterSaleStatusName("待审核");
        item.setRefundAmount(new BigDecimal("99.99"));
        item.setApplyTime(new Date());
        page.setRecords(List.of(item));
        return page;
    }

    private AfterSaleDetailVo createSampleDetail() {
        AfterSaleDetailVo detailVo = new AfterSaleDetailVo();
        detailVo.setId(1L);
        detailVo.setAfterSaleNo("AS20251108001");
        detailVo.setOrderNo("O20251108001");
        detailVo.setAfterSaleType("REFUND_ONLY");
        detailVo.setAfterSaleTypeName("仅退款");
        detailVo.setAfterSaleStatus("PENDING");
        detailVo.setAfterSaleStatusName("待审核");
        detailVo.setRefundAmount(new BigDecimal("99.99"));
        return detailVo;
    }

    private static class StubMallAfterSaleService implements MallAfterSaleService {

        private Page<AfterSaleListVo> page = new Page<>();
        private AfterSaleDetailVo detail;

        private boolean listAfterSalesInvoked;
        private boolean getAfterSaleDetailInvoked;

        private MallAfterSaleListRequest capturedRequest;
        private Long capturedAfterSaleId;

        @Override
        public Page<AfterSaleListVo> listAfterSales(MallAfterSaleListRequest request) {
            this.listAfterSalesInvoked = true;
            this.capturedRequest = request;
            return page;
        }

        @Override
        public AfterSaleDetailVo getAfterSaleDetail(Long afterSaleId) {
            this.getAfterSaleDetailInvoked = true;
            this.capturedAfterSaleId = afterSaleId;
            return detail;
        }
    }
}
