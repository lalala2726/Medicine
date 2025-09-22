package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.common.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.mapper.MedicineMapper;
import cn.zhangchuangla.medicine.model.entity.Medicine;
import cn.zhangchuangla.medicine.model.entity.MedicineCategory;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineUpdateRequest;
import cn.zhangchuangla.medicine.service.MedicineCategoryService;
import cn.zhangchuangla.medicine.service.MedicineService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 药品服务实现类
 *
 * @author Chuang
 * created on 2025/9/22 13:35
 */
@Service
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine> implements MedicineService {

    private final MedicineCategoryService medicineCategoryService;

    public MedicineServiceImpl(MedicineCategoryService medicineCategoryService) {
        this.medicineCategoryService = medicineCategoryService;
    }

    @Override
    public Page<Medicine> listMedicine(MedicineListQueryRequest request) {
        Page<Medicine> page = new Page<>(request.getPageNum(), request.getPageSize());
        return baseMapper.listMedicine(page, request);
    }

    /**
     * 检查分类是否存在
     *
     * @param categoryId 分类ID
     */
    private void checkCategoryExists(Long categoryId) {
        MedicineCategory category = medicineCategoryService.getById(categoryId);
        Assert.notNull(category, "分类不存在");
    }

    /**
     * 检查药品名称是否重复（排除指定ID）
     *
     * @param name       药品名称
     * @param excludeId  排除的药品ID（用于更新时排除自身）
     * @return 重复的药品对象，如果不存在返回null
     */
    private Medicine checkMedicineNameDuplicate(String name, Long excludeId) {
        LambdaQueryChainWrapper<Medicine> wrapper = lambdaQuery()
                .eq(Medicine::getName, name)
                .or()
                .eq(Medicine::getGenericName, name);

        if (excludeId != null) {
            wrapper.ne(Medicine::getId, excludeId);
        }

        return wrapper.one();
    }

    /**
     * 检查药品通用名是否重复（排除指定ID）
     *
     * @param genericName 药品通用名
     * @param excludeId   排除的药品ID（用于更新时排除自身）
     * @return 重复的药品对象，如果不存在返回null
     */
    private Medicine checkMedicineGenericNameDuplicate(String genericName, Long excludeId) {
        LambdaQueryChainWrapper<Medicine> wrapper = lambdaQuery()
                .eq(Medicine::getGenericName, genericName);

        if (excludeId != null) {
            wrapper.ne(Medicine::getId, excludeId);
        }

        return wrapper.one();
    }

    @Override
    public Medicine getMedicineById(Long id) {
        return getById(id);
    }

    @Override
    public boolean addMedicine(MedicineAddRequest request) {
        Assert.notNull(request, "药品添加请求对象不能为空");

        //1.检查分类是否存在
        checkCategoryExists(request.getCategoryId());

        //2. 检查药品名称是否与其他药品重复
        Medicine existingMedicine = checkMedicineNameDuplicate(request.getName(), null);
        Assert.isNull(existingMedicine, "药品名称已存在");

        // 拷贝属性并创建药品
        Medicine medicine = BeanCotyUtils.copyProperties(request, Medicine.class);
        medicine.setCreateTime(new Date());
        medicine.setUpdateTime(new Date());

        return save(medicine);
    }

    @Override
    public boolean updateMedicine(MedicineUpdateRequest request) {
        Assert.notNull(request, "药品修改请求对象不能为空");
        Assert.notNull(request.getId(), "药品ID不能为空");

        // 检查药品是否存在
        Medicine existingMedicine = getById(request.getId());
        Assert.notNull(existingMedicine, "药品不存在");

        // 检查分类是否存在
        if (request.getCategoryId() != null) {
            checkCategoryExists(request.getCategoryId());
        }

        // 检查药品名称是否与其他药品重复
        if (request.getName() != null && !request.getName().equals(existingMedicine.getName())) {
            Medicine duplicateMedicine = checkMedicineNameDuplicate(request.getName(), request.getId());
            Assert.isNull(duplicateMedicine, "药品名称已存在");
        }

        // 检查通用名是否与其他药品重复
        if (request.getGenericName() != null && !request.getGenericName().equals(existingMedicine.getGenericName())) {
            Medicine duplicateMedicine = checkMedicineGenericNameDuplicate(request.getGenericName(), request.getId());
            Assert.isNull(duplicateMedicine, "药品通用名已存在");
        }

        // 拷贝属性并更新药品
        Medicine medicine = BeanCotyUtils.copyProperties(request, Medicine.class);
        medicine.setUpdateTime(new Date());

        return updateById(medicine);
    }

    @Override
    public boolean deleteMedicine(List<Long> ids) {
        Assert.notNull(ids, "药品ID列表不能为空");
        Assert.notEmpty(ids, "药品ID列表不能为空");

        // 检查药品是否存在
        for (Long id : ids) {
            Medicine medicine = getById(id);
            Assert.notNull(medicine, "ID为 " + id + " 的药品不存在");
        }

        return removeByIds(ids);
    }

}
