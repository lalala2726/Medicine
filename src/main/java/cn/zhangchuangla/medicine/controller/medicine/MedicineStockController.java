package cn.zhangchuangla.medicine.controller.medicine;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.common.base.TableDataResult;
import cn.zhangchuangla.medicine.model.entity.MedicineStock;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineStockAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineStockQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineStockUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.medicine.MedicineStockVo;
import cn.zhangchuangla.medicine.service.MedicineStockService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 药品库存控制器
 *
 * @author Chuang
 * created on 2025/9/22 15:55
 */
@RestController
@RequestMapping("/medicine/stock")
@RequiredArgsConstructor
@Tag(name = "药品库存接口", description = "提供药品库存的增删改查")
public class MedicineStockController extends BaseController {

    private final MedicineStockService medicineStockService;

    /**
     * 获取药品库存列表
     *
     * @param request 查询参数
     * @return 药品库存列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取药品库存列表")
    public AjaxResult<TableDataResult> listMedicineStock(MedicineStockQueryRequest request) {
        Page<MedicineStock> page = medicineStockService.listMedicineStock(request);
        List<MedicineStockVo> stockVos = copyListProperties(page, MedicineStockVo.class);
        return getTableData(page, stockVos);
    }

    /**
     * 获取药品库存详情
     *
     * @param id 库存ID
     * @return 药品库存详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取药品库存详情")
    public AjaxResult<MedicineStockVo> getMedicineStockById(@PathVariable("id") Long id) {
        MedicineStock stock = medicineStockService.getMedicineStockById(id);
        MedicineStockVo stockVo = copyProperties(stock, MedicineStockVo.class);
        return success(stockVo);
    }

    /**
     * 添加药品库存
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加药品库存")
    public AjaxResult<Void> addMedicineStock(@RequestBody MedicineStockAddRequest request) {
        boolean result = medicineStockService.addMedicineStock(request);
        return toAjax(result);
    }

    /**
     * 修改药品库存
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改药品库存")
    public AjaxResult<Void> updateMedicineStock(@RequestBody MedicineStockUpdateRequest request) {
        boolean result = medicineStockService.updateMedicineStock(request);
        return toAjax(result);
    }

    /**
     * 删除药品库存
     *
     * @param ids 库存ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除药品库存")
    public AjaxResult<Void> deleteMedicineStock(@PathVariable("ids") List<Long> ids) {
        boolean result = medicineStockService.deleteMedicineStock(ids);
        return toAjax(result);
    }

}
