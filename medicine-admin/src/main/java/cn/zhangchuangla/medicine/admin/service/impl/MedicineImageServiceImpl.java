package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MedicineImageMapper;
import cn.zhangchuangla.medicine.admin.model.entity.MedicineImage;
import cn.zhangchuangla.medicine.admin.service.MedicineImageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 药品图片服务实现类
 *
 * @author Chuang
 * created on 2025/9/22 14:10
 */
@Service
public class MedicineImageServiceImpl extends ServiceImpl<MedicineImageMapper, MedicineImage> implements MedicineImageService {

    @Override
    public List<MedicineImage> getImagesByMedicineId(Long medicineId) {
        return lambdaQuery()
                .eq(MedicineImage::getMedicineId, medicineId)
                .orderByAsc(MedicineImage::getSort)
                .orderByDesc(MedicineImage::getCreateTime)
                .list();
    }

    @Override
    public boolean deleteImagesByMedicineId(Long medicineId) {
        return lambdaUpdate()
                .eq(MedicineImage::getMedicineId, medicineId)
                .remove();
    }

}




