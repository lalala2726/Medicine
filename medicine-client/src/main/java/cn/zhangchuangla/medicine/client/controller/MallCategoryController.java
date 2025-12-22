package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.service.MallCategoryService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.mall.MallCategoryTree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商城商品分类前台接口
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/mall/category")
@RequiredArgsConstructor
@Tag(name = "商城商品分类前台接口")
public class MallCategoryController extends BaseController {

    private final MallCategoryService mallCategoryService;

    /**
     * 获取商品分类树（仅启用分类）
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
