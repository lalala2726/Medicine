package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.vo.MallProductVo;
import cn.zhangchuangla.medicine.admin.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.mall.MallProductAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.mall.MallProductListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城商品控制器
 * <p>
 * 提供商城商品的增删改查功能，包括商品列表查询、商品详情查询、
 * 商品添加、商品修改和商品删除等功能。
 *
 * @author Chuang
 * created on 2025/10/4
 */
@RestController
@RequestMapping("/mall/product")
@RequiredArgsConstructor
@IsAdmin
@Tag(name = "商城商品接口", description = "提供商城商品的增删改查")
public class MallProductController extends BaseController {

    private final MallProductService mallProductService;

    /**
     * 获取商城商品列表
     *
     * @param request 查询参数
     * @return 商城商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商城商品列表")
    public AjaxResult<TableDataResult> listMallProduct(MallProductListQueryRequest request) {
        Page<MallProductDetailDto> page = mallProductService.listMallProductWithCategory(request);
        List<MallProductListVo> mallProductListVos = page.getRecords().stream()
                .map(product -> {
                    MallProductListVo productListVo = copyProperties(product, MallProductListVo.class);
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        productListVo.setCoverImage(product.getImages().getFirst());
                    }
                    return productListVo;
                })
                .toList();
        return getTableData(page, mallProductListVos);
    }

    /**
     * 获取商城商品详情
     *
     * @param id 商品ID
     * @return 商城商品详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取商城商品详情")
    public AjaxResult<MallProductVo> getProductById(@PathVariable("id") Long id) {
        MallProductDetailDto product = mallProductService.getMallProductById(id);
        MallProductVo productVo = copyProperties(product, MallProductVo.class);
        return success(productVo);
    }

    /**
     * 添加商城商品
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加商城商品")
    public AjaxResult<Void> addProduct(@Validated @RequestBody MallProductAddRequest request) {
        boolean result = mallProductService.addMallProduct(request);
        return toAjax(result);
    }

    /**
     * 修改商城商品
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改商城商品")
    public AjaxResult<Void> updateProduct(@Validated @RequestBody MallProductUpdateRequest request) {
        boolean result = mallProductService.updateMallProduct(request);
        return toAjax(result);
    }

    /**
     * 删除商城商品
     *
     * @param ids 商品ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除商城商品")
    public AjaxResult<Void> deleteProduct(@PathVariable("ids") List<Long> ids) {
        boolean result = mallProductService.deleteMallProduct(ids);
        return toAjax(result);
    }

    /**
     * 获取售后列表
     */
    @GetMapping("/after-sale/list")
    @Operation
    public AjaxResult<TableDataResult> listAfterSale() {
        return success();
    }

    /**
     * 获取售后详情
     */
    @GetMapping("/after-sale/{id:\\d+}")
    @Operation(summary = "获取售后详情")
    public AjaxResult<Void> getAfterSaleById(@PathVariable("id") Long id) {
        return success();
    }

}
