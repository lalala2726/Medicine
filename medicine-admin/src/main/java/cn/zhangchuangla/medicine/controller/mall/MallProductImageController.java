package cn.zhangchuangla.medicine.controller.mall;

import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.common.core.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.common.base.TableDataResult;
import cn.zhangchuangla.medicine.common.core.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductImageAddRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductImageListQueryRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductImageUpdateRequest;
import cn.zhangchuangla.medicine.common.core.model.vo.mall.MallProductImageVo;
import cn.zhangchuangla.medicine.service.MallProductImageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城商品图片接口
 * <p>
 * 提供商城商品图片的增删改查功能，包括图片上传、排序、
 * 商品关联管理等操作。
 *
 * @author Chuang
 * created on 2025/10/4 02:20
 */
@RestController
@RequestMapping("/mall/product/image")
@RequiredArgsConstructor
@Tag(name = "商城商品图片接口", description = "提供商城商品图片的增删改查")
public class MallProductImageController extends BaseController {

    private final MallProductImageService mallProductImageService;

    /**
     * 获取商城商品图片列表
     *
     * @param request 查询参数
     * @return 商城商品图片列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商城商品图片列表")
    public AjaxResult<TableDataResult> listMallProductImage(MallProductImageListQueryRequest request) {
        Page<MallProductImage> page = mallProductImageService.listImagesByProductId(request);
        List<MallProductImageVo> imageListVos = copyListProperties(page, MallProductImageVo.class);
        return getTableData(page, imageListVos);
    }

    /**
     * 根据商品ID获取图片列表
     */
    @GetMapping("/list/{productId}")
    @Operation(summary = "根据商品ID获取图片列表", description = "根据商品ID获取该商品的所有图片，按排序和创建时间排序")
    public AjaxResult<List<MallProductImageVo>> listByProductId(
            @Parameter(description = "商品ID", required = true)
            @PathVariable Long productId) {
        List<MallProductImage> images = mallProductImageService.listImagesByProductId(productId);
        List<MallProductImageVo> voList = images.stream()
                .map(image -> {
                    MallProductImageVo vo = new MallProductImageVo();
                    BeanUtils.copyProperties(image, vo);
                    return vo;
                })
                .toList();
        return success(voList);
    }

    /**
     * 获取商城商品图片详情
     *
     * @param id 图片ID
     * @return 商城商品图片详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取商城商品图片详情")
    public AjaxResult<MallProductImageVo> getImageById(@PathVariable("id") Long id) {
        MallProductImage image = mallProductImageService.getById(id);
        MallProductImageVo imageVo = copyProperties(image, MallProductImageVo.class);
        return success(imageVo);
    }

    /**
     * 添加商城商品图片
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加商城商品图片")
    public AjaxResult<Void> addImage(@RequestBody MallProductImageAddRequest request) {
        boolean result = mallProductImageService.addImage(request);
        return toAjax(result);
    }

    /**
     * 修改商城商品图片
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改商城商品图片")
    public AjaxResult<Void> updateImage(@RequestBody MallProductImageUpdateRequest request) {
        boolean result = mallProductImageService.updateImage(request);
        return toAjax(result);
    }

    /**
     * 删除商城商品图片
     *
     * @param ids 图片ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除商城商品图片")
    public AjaxResult<Void> deleteImage(@PathVariable("ids") List<Long> ids) {
        boolean result = mallProductImageService.deleteImage(ids);
        return toAjax(result);
    }
}
