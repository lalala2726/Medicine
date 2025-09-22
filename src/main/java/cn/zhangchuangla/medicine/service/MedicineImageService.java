package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.model.entity.MedicineImage;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineImageAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineImageListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineImageUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 药品图片服务接口
 *
 * @author Chuang
 * created on 2025/9/22 14:04
 */
public interface MedicineImageService extends IService<MedicineImage> {

    /**
     * 分页查询药品图片列表
     *
     * @param request 查询参数
     * @return 药品图片分页列表
     */
    Page<MedicineImage> listMedicineImage(MedicineImageListQueryRequest request);

    /**
     * 根据ID获取药品图片详情
     *
     * @param id 图片ID
     * @return 药品图片详情
     */
    MedicineImage getMedicineImageById(Long id);

    /**
     * 根据药品ID获取图片列表
     *
     * @param medicineId 药品ID
     * @return 药品图片列表
     */
    List<MedicineImage> getImagesByMedicineId(Long medicineId);

    /**
     * 添加药品图片
     *
     * @param request 添加参数
     * @return 是否成功
     */
    boolean addMedicineImage(MedicineImageAddRequest request);

    /**
     * 修改药品图片
     *
     * @param request 修改参数
     * @return 是否成功
     */
    boolean updateMedicineImage(MedicineImageUpdateRequest request);

    /**
     * 删除药品图片
     *
     * @param ids 图片ID列表
     * @return 是否成功
     */
    boolean deleteMedicineImage(List<Long> ids);

    /**
     * 根据药品ID删除所有图片
     *
     * @param medicineId 药品ID
     * @return 是否成功
     */
    boolean deleteImagesByMedicineId(Long medicineId);

}