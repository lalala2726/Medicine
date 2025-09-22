package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.mapper.MedicineMapper;
import cn.zhangchuangla.medicine.model.entity.Medicine;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineUpdateRequest;
import cn.zhangchuangla.medicine.service.MedicineService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 药品服务实现类
 *
 * @author Chuang
 * created on 2025/9/22 13:35
 */
@Service
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine>
        implements MedicineService {

    @Override
    public Page<Medicine> listMedicine(MedicineListQueryRequest request) {
        Page<Medicine> page = new Page<>(request.getPageNum(), request.getPageSize());
        return baseMapper.listMedicine(page, request);
    }

    @Override
    public Medicine getMedicineById(Long id) {
        return getById(id);
    }

    @Override
    public boolean addMedicine(MedicineAddRequest request) {
        Medicine medicine = BeanCotyUtils.copyProperties(request, Medicine.class);
        return save(medicine);
    }

    @Override
    public boolean updateMedicine(MedicineUpdateRequest request) {
        Medicine medicine = BeanCotyUtils.copyProperties(request, Medicine.class);
        return updateById(medicine);
    }

    @Override
    public boolean deleteMedicine(List<Long> ids) {
        return removeByIds(ids);
    }

}
