package cn.zhangchuangla.medicine.controller.mall;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.common.base.TableDataResult;
import cn.zhangchuangla.medicine.model.entity.MallProductDetail;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.mall.MallProductDetailVo;
import cn.zhangchuangla.medicine.service.MallProductDetailService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城商品详情接口
 * <p>
 * 提供商城商品详情的增删改查功能，包括详情查询、详情添加、
 * 详情修改和详情删除等功能。业务逻辑在Service层处理。
 *
 * @author Chuang
 * created on 2025/10/4 02:35
 */
@RestController
@RequestMapping("/mall/product/detail")
@RequiredArgsConstructor
@Tag(name = "商城商品详情接口", description = "提供商城商品详情的增删改查")
public class MallProductDetailController extends BaseController {

    private final MallProductDetailService mallProductDetailService;

    /**
     * 获取商城商品详情列表
     *
     * @param request 查询参数
     * @return 商城商品详情列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商城商品详情列表")
    public AjaxResult<TableDataResult> listMallProductDetail(MallProductDetailListQueryRequest request) {
        Page<MallProductDetail> page = mallProductDetailService.listDetailsByProductId(request);
        List<MallProductDetailVo> detailListVos = copyListProperties(page, MallProductDetailVo.class);
        return getTableData(page, detailListVos);
    }

    /**
     * 获取商城商品详情详情
     *
     * @param id 详情ID
     * @return 商城商品详情详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取商城商品详情详情")
    public AjaxResult<MallProductDetailVo> getDetailById(@PathVariable("id") Long id) {
        MallProductDetail detail = mallProductDetailService.getById(id);
        MallProductDetailVo detailVo = copyProperties(detail, MallProductDetailVo.class);
        return success(detailVo);
    }

    /**
     * 添加商城商品详情
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加商城商品详情")
    public AjaxResult<Void> addDetail(@RequestBody MallProductDetailAddRequest request) {
        boolean result = mallProductDetailService.addDetail(request);
        return toAjax(result);
    }

    /**
     * 修改商城商品详情
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改商城商品详情")
    public AjaxResult<Void> updateDetail(@RequestBody MallProductDetailUpdateRequest request) {
        boolean result = mallProductDetailService.updateDetail(request);
        return toAjax(result);
    }

    /**
     * 删除商城商品详情
     *
     * @param ids 详情ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除商城商品详情")
    public AjaxResult<Void> deleteDetail(@PathVariable("ids") List<Long> ids) {
        boolean result = mallProductDetailService.deleteDetail(ids);
        return toAjax(result);
    }
}
