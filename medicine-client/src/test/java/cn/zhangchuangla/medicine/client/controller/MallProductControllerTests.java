package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.client.model.vo.MallProductVo;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.client.service.MallUserBrowseHistoryService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MallProductControllerTests {

    @Mock
    private MallProductService mallProductService;
    @Mock
    private MallUserBrowseHistoryService mallUserBrowseHistoryService;

    private MallProductController controller;

    @BeforeEach
    void setUp() {
        controller = new MallProductController(mallProductService, mallUserBrowseHistoryService) {
            @Override
            protected Long getUserId() {
                return 100L;
            }
        };
    }

    /**
     * 场景：获取商品详情时返回成功结果并记录浏览行为。
     */
    @Test
    void getMallProductByIdReturnsVoAndRecordsView() {
        Long productId = 10L;

        MallProduct mallProduct = new MallProduct();
        mallProduct.setId(productId);
        mallProduct.setName("测试商品");
        mallProduct.setUnit("盒");
        mallProduct.setPrice(new BigDecimal("88.50"));
        mallProduct.setSalesVolume(321L);

        when(mallProductService.getMallProductById(productId)).thenReturn(mallProduct);

        AjaxResult<MallProductVo> result = controller.getMallProductById(productId);

        // 打印响应数据，便于调试查看
        System.out.println("商品详情接口响应数据：" + result);
        System.out.println("商品详情：" + result.getData());

        verify(mallProductService).getMallProductById(productId);
        verify(mallUserBrowseHistoryService).recordProductBrowse(100L, productId);
        verify(mallProductService).recordView(productId);

        assertEquals(ResponseCode.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        assertEquals(mallProduct.getId(), result.getData().getId());
        assertEquals(mallProduct.getName(), result.getData().getName());
        assertEquals(mallProduct.getUnit(), result.getData().getUnit());
        assertEquals(mallProduct.getPrice(), result.getData().getPrice());
        assertEquals(mallProduct.getSalesVolume(), result.getData().getSalesVolume());
    }

    /**
     * 场景：查询商品浏览量返回统计数。
     */
    @Test
    void getProductViewsReturnsViewCount() {
        Long productId = 20L;
        long viewCount = 88L;

        when(mallProductService.getViewCount(productId, ProductViewPeriod.TOTAL)).thenReturn(viewCount);

        AjaxResult<Long> result = controller.getProductViews(productId, "total");

        // 打印响应数据，便于调试查看
        System.out.println("商品浏览量接口响应数据：" + result);
        System.out.println("浏览量：" + result.getData());

        verify(mallProductService).getViewCount(productId, ProductViewPeriod.TOTAL);

        assertEquals(ResponseCode.SUCCESS.getCode(), result.getCode());
        assertEquals(viewCount, result.getData());
    }
}
