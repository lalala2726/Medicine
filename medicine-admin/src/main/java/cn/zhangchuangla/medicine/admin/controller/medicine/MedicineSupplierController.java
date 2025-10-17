package cn.zhangchuangla.medicine.admin.controller.medicine;

import cn.zhangchuangla.medicine.admin.model.request.medicine.SupplierAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.medicine.SupplierListQueryRequest;
import cn.zhangchuangla.medicine.admin.model.request.medicine.SupplierUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.medicine.SupplierListVo;
import cn.zhangchuangla.medicine.admin.model.vo.medicine.SupplierVo;
import cn.zhangchuangla.medicine.admin.service.MedicineSupplierService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.Option;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.MedicineSupplier;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商控制器
 */
@RestController
@RequestMapping("/medicine/supplier")
@RequiredArgsConstructor
@Tag(name = "供应商接口", description = "提供供应商的增删改查")
public class MedicineSupplierController extends BaseController {

    private final MedicineSupplierService medicineSupplierService;

    /**
     * 分页查询供应商列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取供应商列表")
    public AjaxResult<TableDataResult> listSupplier(SupplierListQueryRequest request) {
        Page<MedicineSupplier> page = medicineSupplierService.listSupplier(request);
        List<SupplierListVo> supplierListVos = copyListProperties(page, SupplierListVo.class);
        return getTableData(page, supplierListVos);
    }

    /**
     * 获取供应商选项
     */
    @GetMapping("/option")
    @Operation(summary = "获取供应商选项")
    public AjaxResult<List<Option<Long>>> option() {
        List<Option<Long>> options = medicineSupplierService.option();
        return success(options);
    }

    /**
     * 根据ID查询供应商详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取供应商详情")
    public AjaxResult<SupplierVo> getSupplierById(@PathVariable("id") Long id) {
        MedicineSupplier medicineSupplier = medicineSupplierService.getSupplierById(id);
        SupplierVo supplierVo = copyProperties(medicineSupplier, SupplierVo.class);
        return success(supplierVo);
    }

    /**
     * 添加供应商
     */
    @PostMapping
    @Operation(summary = "添加供应商")
    public AjaxResult<Void> addSupplier(@RequestBody SupplierAddRequest request) {
        boolean result = medicineSupplierService.addSupplier(request);
        return toAjax(result);
    }

    /**
     * 更新供应商
     */
    @PutMapping
    @Operation(summary = "更新供应商")
    public AjaxResult<Void> updateSupplier(@RequestBody SupplierUpdateRequest request) {
        boolean result = medicineSupplierService.updateSupplier(request);
        return toAjax(result);
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除供应商")
    public AjaxResult<Void> deleteSupplier(@PathVariable("ids") List<Long> ids) {
        boolean result = medicineSupplierService.deleteSupplier(ids);
        return toAjax(result);
    }
}
