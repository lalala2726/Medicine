package cn.zhangchuangla.medicine.client.controller.mall;

import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.client.model.vo.MallProductVo;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.client.service.MallUserBrowseHistoryService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商城商品前台接口
 *
 * <p>聚焦商品详情、浏览统计等读操作。</p>
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/mall/product")
@RequiredArgsConstructor
@Validated
public class MallProductController extends BaseController {

    private final MallProductService mallProductService;
    private final MallUserBrowseHistoryService mallUserBrowseHistoryService;

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public AjaxResult<MallProductVo> getMallProductById(@Min(value = 1, message = "商品ID不能小于1")
                                                        @PathVariable("id") Long id) {
        MallProduct mallProduct = mallProductService.getMallProductById(id);
        MallProductVo mallProductVo = BeanCotyUtils.copyProperties(mallProduct, MallProductVo.class);
        mallUserBrowseHistoryService.recordProductBrowse(getUserId(), id);
        // 加一笔浏览记录，兼容用户直接查看详情的场景
        mallProductService.recordView(id);
        return success(mallProductVo);
    }


    /**
     * 查询商品浏览量，可按小时/天/周/月等维度统计
     *
     * @param id     商品ID
     * @param period 统计周期（hour、day、week、month、total）
     * @return 浏览次数
     */
    @GetMapping("/{id}/views")
    @Operation(summary = "查询商品浏览量")
    @Anonymous
    public AjaxResult<Long> getProductViews(@Min(value = 1, message = "商品ID不能小于1")
                                            @PathVariable("id") Long id,
                                            @Parameter(description = "统计周期（hour、day、week、month、total）")
                                            @RequestParam(value = "period", required = false, defaultValue = "total")
                                            String period) {
        ProductViewPeriod viewPeriod = ProductViewPeriod.fromCode(period);
        long count = mallProductService.getViewCount(id, viewPeriod);
        return success(count);
    }
}
