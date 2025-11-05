package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.service.MallRecommendService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.product.RecommendRequest;
import cn.zhangchuangla.medicine.model.vo.RecommendListVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MallRecommendControllerTests {

    @Mock
    private MallRecommendService mallRecommendService;

    private MallRecommendController controller;

    @BeforeEach
    void setUp() {
        controller = new MallRecommendController(mallRecommendService);
    }

    /**
     * 场景：推荐接口返回成功结果并完成实体到视图对象的转换。
     */
    @Test
    void recommendReturnsSuccessResponseWithConvertedList() {
        RecommendRequest request = new RecommendRequest();
        request.setSize(5);
        request.setHotCursor(2);

        MallProduct mallProduct = new MallProduct();
        mallProduct.setPrice(new BigDecimal("19.99"));
        mallProduct.setSalesVolume(42L);

        when(mallRecommendService.recommend(request)).thenReturn(List.of(mallProduct));

        AjaxResult<List<RecommendListVo>> result = controller.recommend(request);

        // 打印响应数据，便于调试查看
        System.out.println("推荐接口响应数据：" + result);
        System.out.println("推荐列表：" + result.getData());

        verify(mallRecommendService).recommend(request);
        assertEquals(ResponseResultCode.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());

        RecommendListVo vo = result.getData().getFirst();
        assertEquals(mallProduct.getPrice(), vo.getPrice());
        assertEquals(mallProduct.getSalesVolume(), vo.getSalesVolume());
    }
}
