package cn.zhangchuangla.medicine.controller.mall;

import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.core.model.entity.MallProductSku;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductSkuAddRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductSkuListQueryRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductSkuUpdateRequest;
import cn.zhangchuangla.medicine.common.core.model.vo.mall.MallProductSkuVo;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.service.MallProductSkuService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城商品SKU规格接口
 * <p>
 * 提供商城商品SKU规格的增删改查功能，包括SKU管理、
 * 规格配置、库存管理、价格设置等操作。
 *
 * @author Chuang
 * created on 2025/10/4 02:24
 */
@RestController
@RequestMapping("/mall/product/sku")
@RequiredArgsConstructor
@Tag(name = "商城商品SKU规格接口", description = "提供商城商品SKU规格的增删改查")
public class MallProductSkuController extends BaseController {

    private final MallProductSkuService mallProductSkuService;

    /**
     * 获取商城商品SKU规格列表
     *
     * @param request 查询参数
     * @return 商城商品SKU规格列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商城商品SKU规格列表")
    public AjaxResult<TableDataResult> listMallProductSku(MallProductSkuListQueryRequest request) {
        Page<MallProductSku> page = mallProductSkuService.listSkusByProductId(request);
        List<MallProductSkuVo> skuListVos = copyListProperties(page, MallProductSkuVo.class);
        return getTableData(page, skuListVos);
    }

    /**
     * 根据商品ID获取SKU列表
     */
    @GetMapping("/list/{productId}")
    @Operation(summary = "根据商品ID获取SKU列表", description = "根据商品ID获取该商品的所有SKU，按排序和创建时间排序")
    public AjaxResult<List<MallProductSkuVo>> listByProductId(
            @Parameter(description = "商品ID", required = true)
            @PathVariable Long productId) {
        List<MallProductSku> skus = mallProductSkuService.listSkusByProductId(productId);
        List<MallProductSkuVo> voList = skus.stream()
                .map(sku -> {
                    MallProductSkuVo vo = new MallProductSkuVo();
                    BeanUtils.copyProperties(sku, vo);
                    return vo;
                })
                .toList();
        return success(voList);
    }

    /**
     * 获取商城商品SKU规格详情
     *
     * @param id SKU规格ID
     * @return 商城商品SKU规格详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取商城商品SKU规格详情")
    public AjaxResult<MallProductSkuVo> getSkuById(@PathVariable("id") Long id) {
        MallProductSku sku = mallProductSkuService.getById(id);
        MallProductSkuVo skuVo = copyProperties(sku, MallProductSkuVo.class);
        return success(skuVo);
    }

    /**
     * 添加商城商品SKU规格
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加商城商品SKU规格")
    public AjaxResult<Void> addSku(@RequestBody MallProductSkuAddRequest request) {
        boolean result = mallProductSkuService.addSku(request);
        return toAjax(result);
    }

    /**
     * 修改商城商品SKU规格
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改商城商品SKU规格")
    public AjaxResult<Void> updateSku(@RequestBody MallProductSkuUpdateRequest request) {
        boolean result = mallProductSkuService.updateSku(request);
        return toAjax(result);
    }

    /**
     * 删除商城商品SKU规格
     *
     * @param ids SKU规格ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除商城商品SKU规格")
    public AjaxResult<Void> deleteSku(@PathVariable("ids") List<Long> ids) {
        boolean result = mallProductSkuService.deleteSku(ids);
        return toAjax(result);
    }
}
