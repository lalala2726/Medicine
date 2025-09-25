package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.base.BaseService;
import cn.zhangchuangla.medicine.common.base.Option;
import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.mapper.MedicineMapper;
import cn.zhangchuangla.medicine.mapper.SupplierMapper;
import cn.zhangchuangla.medicine.model.entity.Medicine;
import cn.zhangchuangla.medicine.model.entity.Supplier;
import cn.zhangchuangla.medicine.model.request.medicine.SupplierAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.SupplierListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.SupplierUpdateRequest;
import cn.zhangchuangla.medicine.service.SupplierService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier>
        implements SupplierService, BaseService {

    private final MedicineMapper medicineMapper;

    @Override
    public Page<Supplier> listSupplier(SupplierListQueryRequest request) {
        Page<Supplier> page = new Page<>(request.getPageNum(), request.getPageSize());
        return baseMapper.listSupplier(page, request);
    }

    @Override
    public Supplier getSupplierById(Long id) {
        Assert.notNull(id, "供应商ID不能为空");
        Supplier supplier = getById(id);
        Assert.notNull(supplier, "供应商不存在");
        return supplier;
    }

    @Override
    public boolean addSupplier(SupplierAddRequest request) {
        Assert.notNull(request, "供应商添加请求对象不能为空");

        Supplier existing = lambdaQuery()
                .eq(Supplier::getName, request.getName())
                .one();
        Assert.isNull(existing, "供应商名称已存在");

        Supplier supplier = copyProperties(request, Supplier.class);
        supplier.setCreateTime(new Date());
        supplier.setUpdateTime(new Date());

        return save(supplier);
    }

    @Override
    public boolean updateSupplier(SupplierUpdateRequest request) {
        Assert.notNull(request, "供应商更新请求对象不能为空");
        Assert.notNull(request.getId(), "供应商ID不能为空");

        Supplier existingSupplier = getById(request.getId());
        Assert.notNull(existingSupplier, "供应商不存在");

        if (!existingSupplier.getName().equals(request.getName())) {
            Supplier duplicate = lambdaQuery()
                    .eq(Supplier::getName, request.getName())
                    .ne(Supplier::getId, request.getId())
                    .one();
            Assert.isNull(duplicate, "供应商名称已存在");
        }

        Supplier supplier = copyProperties(request, Supplier.class);
        supplier.setUpdateTime(new Date());

        return updateById(supplier);
    }

    @Override
    public boolean deleteSupplier(List<Long> ids) {
        Assert.notEmpty(ids, "供应商ID列表不能为空");

        for (Long id : ids) {
            Supplier supplier = getById(id);
            Assert.notNull(supplier, "ID为 " + id + " 的供应商不存在");
        }

        List<Supplier> suppliers = listByIds(ids);
        List<String> supplierNames = suppliers.stream()
                .map(Supplier::getName)
                .toList();

        // 如果该供应商已被药品选择为生产厂家，则不允许删除
        long count = medicineMapper.selectCount(
                new LambdaQueryWrapper<Medicine>().in(Medicine::getManufacturer, supplierNames)
        );
        if (count > 0) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "该供应商已被药品选择为生产厂家，请先解除关联关系");
        }

        return removeByIds(ids);
    }

    @Override
    public List<Option<Long>> option() {
        List<Supplier> list = list();
        return list.stream().map(supplier -> new Option<>(supplier.getId(), supplier.getName())).toList();
    }
}
