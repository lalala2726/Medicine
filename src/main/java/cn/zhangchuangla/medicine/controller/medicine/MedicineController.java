package cn.zhangchuangla.medicine.controller.medicine;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.common.base.TableDataResult;
import cn.zhangchuangla.medicine.model.entity.Medicine;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.medicine.MedicineListVo;
import cn.zhangchuangla.medicine.model.vo.medicine.MedicineVo;
import cn.zhangchuangla.medicine.service.MedicineService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 药品控制器
 *
 * @author Chuang
 * created on 2025/9/22 13:35
 */
@RestController
@RequestMapping("/medicine")
@RequiredArgsConstructor
@Tag(name = "药品接口", description = "提供药品的增删改查")
public class MedicineController extends BaseController {

    private final MedicineService medicineService;

    /**
     * 获取药品列表
     *
     * @param request 查询参数
     * @return 药品列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取药品列表")
    public AjaxResult<TableDataResult> listMedicine(MedicineListQueryRequest request) {
        Page<Medicine> page = medicineService.listMedicine(request);
        List<MedicineListVo> medicineListVos = copyListProperties(page, MedicineListVo.class);
        return getTableData(page, medicineListVos);
    }

    /**
     * 获取药品详情
     *
     * @param id 药品ID
     * @return 药品详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取药品详情")
    public AjaxResult<MedicineVo> getMedicineById(@PathVariable("id") Long id) {
        Medicine medicine = medicineService.getMedicineById(id);
        MedicineVo medicineVo = copyProperties(medicine, MedicineVo.class);
        return success(medicineVo);
    }

    /**
     * 添加药品
     *
     * @param request 添加参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加药品")
    public AjaxResult<Void> addMedicine(@RequestBody MedicineAddRequest request) {
        boolean result = medicineService.addMedicine(request);
        return toAjax(result);
    }

    /**
     * 修改药品
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改药品")
    public AjaxResult<Void> updateMedicine(@RequestBody MedicineUpdateRequest request) {
        boolean result = medicineService.updateMedicine(request);
        return toAjax(result);
    }

    /**
     * 删除药品
     *
     * @param ids 药品ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除药品")
    public AjaxResult<Void> deleteMedicine(@PathVariable("ids") List<Long> ids) {
        boolean result = medicineService.deleteMedicine(ids);
        return toAjax(result);
    }

}