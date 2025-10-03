package cn.zhangchuangla.medicine.controller.mall;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.mall.category.MallCategoryTree;
import cn.zhangchuangla.medicine.service.MallCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/4 01:43
 */
@RestController
@RequestMapping("/mall/category")
@Tag(name = "商城商品分类接口", description = "商城商品分类接口")
public class MallCategoryController extends BaseController {

    private final MallCategoryService mallCategoryService;

    public MallCategoryController(MallCategoryService mallCategoryService) {
        this.mallCategoryService = mallCategoryService;
    }

    /**
     * 商品分类树
     *
     * @return 商品分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取商品分类树")
    public AjaxResult<List<MallCategoryTree>> categoryTree() {
        List<MallCategoryTree> tree = mallCategoryService.categoryTree();
        return success(tree);
    }
}
