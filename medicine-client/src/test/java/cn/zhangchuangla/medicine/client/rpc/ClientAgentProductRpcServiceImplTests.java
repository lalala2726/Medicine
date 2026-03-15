package cn.zhangchuangla.medicine.client.rpc;

import cn.zhangchuangla.medicine.client.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.client.model.vo.MallProductSearchVo;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSpecDto;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientAgentProductRpcServiceImplTests {

    @Mock
    private MallProductService mallProductService;

    @InjectMocks
    private ClientAgentProductRpcServiceImpl service;

    @Test
    void searchProducts_ShouldClampPageSizeAndMapRows() {
        ClientAgentProductSearchRequest request = new ClientAgentProductSearchRequest();
        request.setKeyword("感冒灵");
        request.setCategoryName("感冒药");
        request.setUsage("缓解感冒");
        request.setPageNum(0);
        request.setPageSize(99);

        PageResult<MallProductSearchVo> searchResult = new PageResult<>(1L, 20L, 1L, List.of(
                MallProductSearchVo.builder()
                        .productId(1L)
                        .productName("999感冒灵颗粒")
                        .cover("https://example.com/product.jpg")
                        .price(new BigDecimal("29.90"))
                        .build()
        ));
        when(mallProductService.search(any(MallProductSearchRequest.class))).thenReturn(searchResult);

        PageResult<?> result = service.searchProducts(request);

        ArgumentCaptor<MallProductSearchRequest> captor = ArgumentCaptor.forClass(MallProductSearchRequest.class);
        verify(mallProductService).search(captor.capture());
        assertEquals("感冒灵", captor.getValue().getKeyword());
        assertEquals("感冒药", captor.getValue().getCategoryName());
        assertEquals("缓解感冒", captor.getValue().getEfficacy());
        assertEquals(1, captor.getValue().getPageNum());
        assertEquals(20, captor.getValue().getPageSize());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRows().size());
    }

    @Test
    void searchProducts_WhenSearchServiceReturnsNull_ShouldReturnEmptyPage() {
        when(mallProductService.search(any(MallProductSearchRequest.class))).thenReturn(null);

        PageResult<?> result = service.searchProducts(null);

        assertEquals(1L, result.getPageNum());
        assertEquals(10L, result.getPageSize());
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    void getProductDetail_ShouldUseReadOnlyDrugQuery() {
        MallProductDetailDto detail = new MallProductDetailDto();
        detail.setId(1L);
        detail.setStatus(1);
        when(mallProductService.getProductAndDrugInfoById(1L)).thenReturn(detail);

        MallProductDetailDto result = service.getProductDetail(1L);

        assertSame(detail, result);
        verify(mallProductService).getProductAndDrugInfoById(1L);
        verify(mallProductService, never()).getMallProductDetail(1L);
    }

    @Test
    void getProductSpec_ShouldExtractDrugDetailFields() {
        DrugDetailDto drugDetail = DrugDetailDto.builder()
                .commonName("复方感冒灵颗粒")
                .composition("三叉苦")
                .packaging("10g*9袋")
                .usageMethod("开水冲服")
                .precautions("详见说明书")
                .build();
        MallProductDetailDto detail = new MallProductDetailDto();
        detail.setId(1L);
        detail.setName("999感冒灵颗粒");
        detail.setCategoryName("感冒药");
        detail.setUnit("盒");
        detail.setStatus(1);
        detail.setDrugDetail(drugDetail);
        when(mallProductService.getProductAndDrugInfoById(1L)).thenReturn(detail);

        ClientAgentProductSpecDto result = service.getProductSpec(1L);

        assertEquals(1L, result.getProductId());
        assertEquals("999感冒灵颗粒", result.getProductName());
        assertEquals("10g*9袋", result.getPackaging());
        assertEquals("三叉苦", result.getComposition());
        assertEquals("开水冲服", result.getUsageMethod());
    }

    @Test
    void getProductDetail_WhenProductOffShelf_ShouldThrowNotFound() {
        MallProductDetailDto detail = new MallProductDetailDto();
        detail.setId(1L);
        detail.setStatus(0);
        when(mallProductService.getProductAndDrugInfoById(1L)).thenReturn(detail);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.getProductDetail(1L));

        assertEquals("商品不存在", exception.getMessage());
    }
}
