package cn.zhangchuangla.medicine.controller.mall;

import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.common.core.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.common.base.TableDataResult;
import cn.zhangchuangla.medicine.common.core.model.entity.MallProductShipping;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductShippingAddRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductShippingListQueryRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductShippingUpdateRequest;
import cn.zhangchuangla.medicine.common.core.model.vo.mall.MallProductShippingVo;
import cn.zhangchuangla.medicine.service.MallProductShippingService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城商品运费配置接口
 * <p>
 * 提供商城商品运费模板的增删改查功能，包括运费模板管理、
 * 运费类型设置、价格配置等操作。
 *
 * @author Chuang
 * created on 2025/10/4 02:22
 */
@RestController
@RequestMapping("/mall/product/shipping")
@RequiredArgsConstructor
@Tag(name = "商城商品运费配置接口", description = "提供商城商品运费配置的增删改查")
public class MallProductShippingController extends BaseController {

    private final MallProductShippingService mallProductShippingService;

    /**
     * 获取商城商品运费配置列表
     *
     * @param request 查询参数
     * @return 商城商品运费配置列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商城商品运费配置列表")
    public AjaxResult<TableDataResult> listMallProductShipping(MallProductShippingListQueryRequest request) {
        Page<MallProductShipping> page = mallProductShippingService.listShippingsByProductId(request);
        List<MallProductShippingVo> shippingListVos = copyListProperties(page, MallProductShippingVo.class);
        return getTableData(page, shippingListVos);
    }

    /**
     * 获取商城商品运费配置详情
     *
     * @param id 运费配置ID
     * @return 商城商品运费配置详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取商城商品运费配置详情")
    public AjaxResult<MallProductShippingVo> getShippingById(@PathVariable("id") Long id) {
        MallProductShipping shipping = mallProductShippingService.getById(id);
        MallProductShippingVo shippingVo = copyProperties(shipping, MallProductShippingVo.class);
        return success(shippingVo);
    }

    /**
     * 添加商城商品运费配置
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加商城商品运费配置")
    public AjaxResult<Void> addShipping(@RequestBody MallProductShippingAddRequest request) {
        boolean result = mallProductShippingService.addShipping(request);
        return toAjax(result);
    }

    /**
     * 修改商城商品运费配置
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改商城商品运费配置")
    public AjaxResult<Void> updateShipping(@RequestBody MallProductShippingUpdateRequest request) {
        boolean result = mallProductShippingService.updateShipping(request);
        return toAjax(result);
    }

    /**
     * 删除商城商品运费配置
     *
     * @param ids 运费配置ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除商城商品运费配置")
    public AjaxResult<Void> deleteShipping(@PathVariable("ids") List<Long> ids) {
        boolean result = mallProductShippingService.deleteShipping(ids);
        return toAjax(result);
    }
}
