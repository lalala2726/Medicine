package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.service.MallRecommendService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.product.RecommendRequest;
import cn.zhangchuangla.medicine.model.vo.mall.RecommendListVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16 15:31
 */
@RestController
@RequestMapping("/mall/product/recommend")
@RequiredArgsConstructor
public class MallRecommendController extends BaseController {

    private final MallRecommendService mallRecommendService;


    /**
     * 商品推荐
     *
     * @param request 请求参数
     * @return 商品列表
     */
    @GetMapping
    @Operation(summary = "商品推荐")
    public AjaxResult<List<RecommendListVo>> recommend(RecommendRequest request) {
        List<MallProduct> mallProducts = mallRecommendService.recommend(request);
        List<RecommendListVo> recommendListVos = copyListProperties(mallProducts, RecommendListVo.class);
        return success(recommendListVos);
    }

}
