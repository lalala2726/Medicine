package cn.zhangchuangla.medicine.admin.controller.mall;

import cn.zhangchuangla.medicine.admin.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.admin.common.core.base.Option;
import cn.zhangchuangla.medicine.admin.common.security.base.BaseController;
import cn.zhangchuangla.medicine.admin.model.entity.MallCategory;
import cn.zhangchuangla.medicine.admin.model.request.mall.category.MallCategoryAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.mall.category.MallCategoryUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.mall.category.MallCategoryTree;
import cn.zhangchuangla.medicine.admin.model.vo.mall.category.MallCategoryVo;
import cn.zhangchuangla.medicine.admin.service.MallCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城商品分类控制器
 * <p>
 * 提供商城商品分类的增删改查功能，包括分类列表查询、分类树结构获取、
 * 分类详情查询、分类添加、分类修改和分类删除等功能。
 *
 * @author Chuang
 * created on 2025/10/4 01:43
 */
@RestController
@RequestMapping("/mall/category")
@RequiredArgsConstructor
@Tag(name = "商城商品分类接口", description = "提供商城商品分类的增删改查")
public class MallCategoryController extends BaseController {

    private final MallCategoryService mallCategoryService;

    /**
     * 获取商品分类树
     *
     * @return 商品分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取商品分类树")
    public AjaxResult<List<MallCategoryTree>> categoryTree() {
        List<MallCategoryTree> tree = mallCategoryService.categoryTree();
        return success(tree);
    }

    /**
     * 获取商品下拉树
     *
     * @return 商品分类树
     */
    @GetMapping("/option")
    @Operation(summary = "获取商品下拉选项")
    public AjaxResult<List<Option<Long>>> option() {
        List<Option<Long>> options = mallCategoryService.option();
        return success(options);
    }

    /**
     * 获取商城商品分类详情
     *
     * @param id 分类ID
     * @return 商城商品分类详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取商城商品分类详情")
    public AjaxResult<MallCategoryVo> getCategoryById(@PathVariable("id") Long id) {
        MallCategory category = mallCategoryService.getCategoryById(id);
        MallCategoryVo categoryVo = copyProperties(category, MallCategoryVo.class);
        return success(categoryVo);
    }

    /**
     * 添加商城商品分类
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加商城商品分类")
    public AjaxResult<Void> addCategory(@Validated @RequestBody MallCategoryAddRequest request) {
        boolean result = mallCategoryService.addCategory(request);
        return toAjax(result);
    }

    /**
     * 修改商城商品分类
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改商城商品分类")
    public AjaxResult<Void> updateCategory(@Validated @RequestBody MallCategoryUpdateRequest request) {
        boolean result = mallCategoryService.updateCategory(request);
        return toAjax(result);
    }

    /**
     * 删除商城商品分类
     *
     * @param ids 分类ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除商城商品分类")
    public AjaxResult<Void> deleteCategory(@PathVariable("ids") List<Long> ids) {
        boolean result = mallCategoryService.deleteCategory(ids);
        return toAjax(result);
    }

}
