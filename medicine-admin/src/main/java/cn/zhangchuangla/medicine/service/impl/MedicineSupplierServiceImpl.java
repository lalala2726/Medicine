package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.core.common.base.Option;
import cn.zhangchuangla.medicine.common.core.common.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.common.utils.Assert;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.model.entity.Medicine;
import cn.zhangchuangla.medicine.common.core.model.entity.MedicineSupplier;
import cn.zhangchuangla.medicine.common.core.model.request.medicine.SupplierAddRequest;
import cn.zhangchuangla.medicine.common.core.model.request.medicine.SupplierListQueryRequest;
import cn.zhangchuangla.medicine.common.core.model.request.medicine.SupplierUpdateRequest;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.mapper.MedicineMapper;
import cn.zhangchuangla.medicine.mapper.SupplierMapper;
import cn.zhangchuangla.medicine.service.MedicineSupplierService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MedicineSupplierServiceImpl extends ServiceImpl<SupplierMapper, MedicineSupplier>
        implements MedicineSupplierService, BaseService {

    private final MedicineMapper medicineMapper;

    @Override
    public Page<MedicineSupplier> listSupplier(SupplierListQueryRequest request) {
        Page<MedicineSupplier> page = new Page<>(request.getPageNum(), request.getPageSize());
        return baseMapper.listSupplier(page, request);
    }

    @Override
    public MedicineSupplier getSupplierById(Long id) {
        Assert.notNull(id, "供应商ID不能为空");
        MedicineSupplier medicineSupplier = getById(id);
        Assert.notNull(medicineSupplier, "供应商不存在");
        return medicineSupplier;
    }

    @Override
    public boolean addSupplier(SupplierAddRequest request) {
        Assert.notNull(request, "供应商添加请求对象不能为空");

        MedicineSupplier existing = lambdaQuery()
                .eq(MedicineSupplier::getName, request.getName())
                .one();
        Assert.isNull(existing, "供应商名称已存在");

        MedicineSupplier medicineSupplier = copyProperties(request, MedicineSupplier.class);
        medicineSupplier.setCreateTime(new Date());
        medicineSupplier.setUpdateTime(new Date());

        return save(medicineSupplier);
    }

    @Override
    public boolean updateSupplier(SupplierUpdateRequest request) {
        Assert.notNull(request, "供应商更新请求对象不能为空");
        Assert.notNull(request.getId(), "供应商ID不能为空");

        MedicineSupplier existingMedicineSupplier = getById(request.getId());
        Assert.notNull(existingMedicineSupplier, "供应商不存在");

        if (!existingMedicineSupplier.getName().equals(request.getName())) {
            MedicineSupplier duplicate = lambdaQuery()
                    .eq(MedicineSupplier::getName, request.getName())
                    .ne(MedicineSupplier::getId, request.getId())
                    .one();
            Assert.isNull(duplicate, "供应商名称已存在");
        }

        MedicineSupplier medicineSupplier = copyProperties(request, MedicineSupplier.class);
        medicineSupplier.setUpdateTime(new Date());

        return updateById(medicineSupplier);
    }

    @Override
    public boolean deleteSupplier(List<Long> ids) {
        Assert.notEmpty(ids, "供应商ID列表不能为空");
        // 检查供应商是否已被药品关联
        LambdaQueryWrapper<MedicineSupplier> supplierLambdaQueryWrapper = new LambdaQueryWrapper<MedicineSupplier>()
                .in(MedicineSupplier::getId, ids);

        Stream<Long> supplierIds = list(supplierLambdaQueryWrapper)
                .stream()
                .map(MedicineSupplier::getId);

        LambdaQueryWrapper<Medicine> medicineLambdaQueryWrapper = new LambdaQueryWrapper<Medicine>()
                .in(Medicine::getSupplierId, supplierIds);
        Long selectCount = medicineMapper.selectCount(medicineLambdaQueryWrapper);
        if (selectCount > 0) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "供应商已被药品关联，请先解除关联关系");
        }
        return removeByIds(ids);
    }

    @Override
    public List<Option<Long>> option() {
        List<MedicineSupplier> list = list();
        return list.stream().map(medicineSupplier -> new Option<>(medicineSupplier.getId(), medicineSupplier.getName())).toList();
    }
}
