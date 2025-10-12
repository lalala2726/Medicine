package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.MedicineImage;
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
     * 根据药品ID获取图片列表
     *
     * @param medicineId 药品ID
     * @return 药品图片列表
     */
    List<MedicineImage> getImagesByMedicineId(Long medicineId);

    /**
     * 根据药品ID删除所有图片
     *
     * @param medicineId 药品ID
     * @return 是否成功
     */
    boolean deleteImagesByMedicineId(Long medicineId);

}
