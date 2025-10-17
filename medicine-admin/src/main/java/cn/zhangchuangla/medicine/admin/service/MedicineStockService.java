package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.medicine.MedicineStockAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.medicine.MedicineStockQueryRequest;
import cn.zhangchuangla.medicine.admin.model.request.medicine.MedicineStockUpdateRequest;
import cn.zhangchuangla.medicine.model.dto.MedicineStockDto;
import cn.zhangchuangla.medicine.model.entity.MedicineStock;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 药品库存服务接口
 *
 * @author Chuang
 * created on 2025/9/22 15:23
 */
public interface MedicineStockService extends IService<MedicineStock> {

    /**
     * 分页查询药品库存列表
     *
     * @param request 查询参数
     * @return 药品库存分页列表
     */
    Page<MedicineStockDto> listMedicineStock(MedicineStockQueryRequest request);

    /**
     * 根据ID获取药品库存详情
     *
     * @param id 库存ID
     * @return 药品库存详情
     */
    MedicineStockDto getMedicineStockById(Long id);

    /**
     * 添加药品库存
     *
     * @param request 添加参数
     * @return 是否成功
     */
    boolean addMedicineStock(MedicineStockAddRequest request);

    /**
     * 修改药品库存
     *
     * @param request 修改参数
     * @return 是否成功
     */
    boolean updateMedicineStock(MedicineStockUpdateRequest request);

    /**
     * 删除药品库存
     *
     * @param ids 库存ID列表
     * @return 是否成功
     */
    boolean deleteMedicineStock(List<Long> ids);

}
