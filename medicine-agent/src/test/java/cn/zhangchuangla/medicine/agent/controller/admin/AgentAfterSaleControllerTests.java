package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.model.vo.admin.AgentAfterSaleListVo;
import cn.zhangchuangla.medicine.agent.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.model.dto.AfterSaleDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallAfterSaleListDto;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentAfterSaleControllerTests {

    private final StubMallAfterSaleService mallAfterSaleService = new StubMallAfterSaleService();
    private final AgentAfterSaleController controller = new AgentAfterSaleController(mallAfterSaleService);

    /**
     * 测试目的：验证列表接口会委托 service 查询，并将 DTO 转换为精简 VO 原始码值结构。
     * 预期结果：响应成功，rows 中 afterSaleType/afterSaleStatus/applyReason 为原始码值，不包含中文映射值。
     */
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

        TableDataResult tableData = result.getData();
        assertNotNull(tableData.getRows());
        assertEquals(1, tableData.getRows().size());

        AgentAfterSaleListVo row = (AgentAfterSaleListVo) tableData.getRows().get(0);
        assertEquals("REFUND_ONLY", row.getAfterSaleType());
        assertEquals("PENDING", row.getAfterSaleStatus());
        assertEquals("DAMAGED", row.getApplyReason());
    }

    /**
     * 测试目的：验证当请求参数为空时，控制器会创建默认请求对象并继续查询流程。
     * 预期结果：响应成功，service 被调用且接收到的请求对象不为空。
     */
    @Test
    void listAfterSales_WithNullRequest_ShouldUseDefault() {
        mallAfterSaleService.page = createSamplePage();

        var result = controller.listAfterSales(null);

        assertEquals(200, result.getCode());
        assertTrue(mallAfterSaleService.listAfterSalesInvoked);
        assertNotNull(mallAfterSaleService.capturedRequest);
    }

    /**
     * 测试目的：验证当 Dubbo 记录被反序列化为 Map 结构时，控制层仍能转换为售后列表 VO。
     * 预期结果：响应成功，rows 第一条记录字段值正确且 afterSaleType/afterSaleStatus/applyReason 为原始码值。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void listAfterSales_WithMapRecord_ShouldConvertSuccessfully() {
        Page<MallAfterSaleListDto> page = new Page<>(1, 10);
        page.setTotal(1);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1L);
        row.put("afterSaleNo", "AS20251108001");
        row.put("orderNo", "O20251108001");
        row.put("userId", 1001L);
        row.put("userNickname", "张三");
        row.put("productName", "感冒药");
        row.put("afterSaleType", "REFUND_ONLY");
        row.put("afterSaleStatus", "PENDING");
        row.put("applyReason", "DAMAGED");
        page.setRecords((List) List.of(row));
        mallAfterSaleService.page = page;

        var result = controller.listAfterSales(new MallAfterSaleListRequest());

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getRows());
        assertEquals(1, result.getData().getRows().size());

        AgentAfterSaleListVo vo = (AgentAfterSaleListVo) result.getData().getRows().get(0);
        assertEquals(1L, vo.getId());
        assertEquals("AS20251108001", vo.getAfterSaleNo());
        assertEquals("REFUND_ONLY", vo.getAfterSaleType());
        assertEquals("PENDING", vo.getAfterSaleStatus());
        assertEquals("DAMAGED", vo.getApplyReason());
    }

    /**
     * 测试目的：验证详情接口会透传售后ID到 service，并返回 service 提供的详情数据。
     * 预期结果：响应成功，service 被调用且返回详情中的售后单号正确。
     */
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
        assertEquals("REFUND_ONLY", result.getData().getAfterSaleType());
        assertEquals("PENDING", result.getData().getAfterSaleStatus());
    }

    /**
     * 测试目的：验证智能体售后列表 VO 不再保留历史 name 字段，确保结构已收敛到精简版本。
     * 预期结果：访问 afterSaleTypeName、afterSaleStatusName、applyReasonName 字段会抛出 NoSuchFieldException。
     */
    @Test
    void agentAfterSaleListVo_ShouldNotContainLegacyNameFields() {
        assertThrows(NoSuchFieldException.class, () -> AgentAfterSaleListVo.class.getDeclaredField("afterSaleTypeName"));
        assertThrows(NoSuchFieldException.class, () -> AgentAfterSaleListVo.class.getDeclaredField("afterSaleStatusName"));
        assertThrows(NoSuchFieldException.class, () -> AgentAfterSaleListVo.class.getDeclaredField("applyReasonName"));
    }

    /**
     * 功能描述：构造售后列表分页模拟数据，供列表相关测试复用。
     *
     * @return 返回包含一条记录的售后分页数据
     * @throws RuntimeException 异常说明：当对象构造过程异常时抛出运行时异常
     */
    private Page<MallAfterSaleListDto> createSamplePage() {
        Page<MallAfterSaleListDto> page = new Page<>(1, 10);
        page.setTotal(1);

        MallAfterSaleListDto item = new MallAfterSaleListDto();
        item.setId(1L);
        item.setAfterSaleNo("AS20251108001");
        item.setOrderId(1L);
        item.setOrderNo("O20251108001");
        item.setOrderItemId(11L);
        item.setUserId(1001L);
        item.setUserNickname("张三");
        item.setProductName("感冒药");
        item.setProductImage("https://example.com/image.jpg");
        item.setAfterSaleType("REFUND_ONLY");
        item.setAfterSaleStatus("PENDING");
        item.setRefundAmount(new BigDecimal("99.99"));
        item.setApplyReason("DAMAGED");
        item.setApplyTime(new Date());
        page.setRecords(List.of(item));
        return page;
    }

    /**
     * 功能描述：构造售后详情模拟数据，供详情接口测试复用。
     *
     * @return 返回售后详情对象
     * @throws RuntimeException 异常说明：当对象构造过程异常时抛出运行时异常
     */
    private AfterSaleDetailDto createSampleDetail() {
        AfterSaleDetailDto detailDto = new AfterSaleDetailDto();
        detailDto.setId(1L);
        detailDto.setAfterSaleNo("AS20251108001");
        detailDto.setOrderNo("O20251108001");
        detailDto.setAfterSaleType("REFUND_ONLY");
        detailDto.setAfterSaleTypeName("仅退款");
        detailDto.setAfterSaleStatus("PENDING");
        detailDto.setAfterSaleStatusName("待审核");
        detailDto.setRefundAmount(new BigDecimal("99.99"));
        return detailDto;
    }

    private static class StubMallAfterSaleService implements MallAfterSaleService {

        private Page<MallAfterSaleListDto> page = new Page<>();
        private AfterSaleDetailDto detail;

        private boolean listAfterSalesInvoked;
        private boolean getAfterSaleDetailInvoked;

        private MallAfterSaleListRequest capturedRequest;
        private Long capturedAfterSaleId;

        @Override
        public Page<MallAfterSaleListDto> listAfterSales(MallAfterSaleListRequest request) {
            this.listAfterSalesInvoked = true;
            this.capturedRequest = request;
            return page;
        }

        @Override
        public AfterSaleDetailDto getAfterSaleDetail(Long afterSaleId) {
            this.getAfterSaleDetailInvoked = true;
            this.capturedAfterSaleId = afterSaleId;
            return detail;
        }
    }
}
