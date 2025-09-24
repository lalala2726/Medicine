package cn.zhangchuangla.medicine.controller.medicine;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.common.base.Option;
import cn.zhangchuangla.medicine.common.base.TableDataResult;
import cn.zhangchuangla.medicine.model.entity.MedicineCategory;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.medicine.CategoryListVo;
import cn.zhangchuangla.medicine.model.vo.medicine.MedicineCategoryVo;
import cn.zhangchuangla.medicine.service.MedicineCategoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 药品分类控制器
 *
 * @author Chuang
 * created on 2025/9/21 19:58
 */
@RestController
@RequestMapping("/medicine/category")
@RequiredArgsConstructor
@Tag(name = "药品分类接口", description = "提供药品分类的增删改查")
public class MedicineCategoryController extends BaseController {

    private final MedicineCategoryService medicineCategoryService;

    /**
     * 获取药品分类列表
     *
     * @param request 查询参数
     * @return 药品分类列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取药品分类列表")
    public AjaxResult<TableDataResult> listMedicineCategory(MedicineCategoryListQueryRequest request) {
        Page<MedicineCategory> page = medicineCategoryService.listMedicineCategory(request);
        List<CategoryListVo> categoryListVos = copyListProperties(page, CategoryListVo.class);
        return getTableData(page, categoryListVos);
    }

    /**
     * 获取药品分类树
     *
     * @return 药品分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取药品分类树")
    public AjaxResult<List<Option<Long>>> tree() {
        List<Option<Long>> options = medicineCategoryService.tree();
        return success(options);
    }

    /**
     * 获取药品分类详情
     *
     * @param id 分类ID
     * @return 药品分类详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取药品分类详情")
    public AjaxResult<MedicineCategoryVo> getCategoryById(@PathVariable("id") Long id) {
        MedicineCategory category = medicineCategoryService.getCategoryById(id);
        MedicineCategoryVo categoryVo = copyProperties(category, MedicineCategoryVo.class);
        return success(categoryVo);
    }

    /**
     * 添加药品分类
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加药品分类")
    public AjaxResult<Void> addCategory(@RequestBody MedicineCategoryAddRequest request) {
        boolean result = medicineCategoryService.addCategory(request);
        return toAjax(result);
    }

    /**
     * 修改药品分类
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改药品分类")
    public AjaxResult<Void> updateCategory(@RequestBody MedicineCategoryUpdateRequest request) {
        boolean result = medicineCategoryService.updateCategory(request);
        return toAjax(result);
    }

    /**
     * 删除药品分类
     *
     * @param ids 分类ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除药品分类")
    public AjaxResult<Void> deleteCategory(@PathVariable("ids") List<Long> ids) {
        boolean result = medicineCategoryService.deleteCategory(ids);
        return toAjax(result);
    }

}
