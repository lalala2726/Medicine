package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.model.entity.Medicine;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 药品服务接口
 *
 * @author Chuang
 * created on 2025/9/22 13:35
 */
public interface MedicineService extends IService<Medicine> {

    /**
     * 分页查询药品列表
     *
     * @param request 查询参数
     * @return 药品分页列表
     */
    Page<Medicine> listMedicine(MedicineListQueryRequest request);

    /**
     * 根据ID获取药品详情
     *
     * @param id 药品ID
     * @return 药品详情
     */
    Medicine getMedicineById(Long id);

    /**
     * 添加药品
     *
     * @param request 添加参数
     * @return 是否成功
     */
    boolean addMedicine(MedicineAddRequest request);

    /**
     * 修改药品
     *
     * @param request 修改参数
     * @return 是否成功
     */
    boolean updateMedicine(MedicineUpdateRequest request);

    /**
     * 删除药品
     *
     * @param ids 药品ID列表
     * @return 是否成功
     */
    boolean deleteMedicine(List<Long> ids);

}
