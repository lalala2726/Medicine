package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallMedicineDetailMapper;
import cn.zhangchuangla.medicine.admin.service.MallMedicineDetailService;
import cn.zhangchuangla.medicine.model.entity.MallMedicineDetail;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Chuang
 */
@Service
public class MallMedicineDetailServiceImpl extends ServiceImpl<MallMedicineDetailMapper, MallMedicineDetail>
        implements MallMedicineDetailService {

    @Override
    public boolean addMedicineDetail(MallMedicineDetail mallMedicineDetail) {
        boolean exists = lambdaQuery()
                .eq(MallMedicineDetail::getProductId, mallMedicineDetail.getProductId())
                .count() > 0;
        if (exists) {
            return updateById(mallMedicineDetail);
        }
        return save(mallMedicineDetail);
    }
}
