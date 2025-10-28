package cn.zhangchuangla.medicine.client.controller.mall;

import cn.zhangchuangla.medicine.client.model.vo.MallProductVo;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.client.service.MallUserBrowseHistoryService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16 16:36
 */
@RestController
@RequestMapping("/mall/product")
@RequiredArgsConstructor
@Validated
public class MallProductController extends BaseController {

    private final MallProductService mallProductService;
    private final MallUserBrowseHistoryService mallUserBrowseHistoryService;


    /**
     * 获取商品信息
     *
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商品信息")
    public AjaxResult<MallProductVo> getMallProductById(@Min(value = 1, message = "商品ID不能小于1")
                                                        @PathVariable("id") Long id) {
        MallProduct mallProduct = mallProductService.getMallProductById(id);
        MallProductVo mallProductVo = BeanCotyUtils.copyProperties(mallProduct, MallProductVo.class);
        mallUserBrowseHistoryService.recordProductBrowse(getUserId(), id);
        return success(mallProductVo);
    }
}
