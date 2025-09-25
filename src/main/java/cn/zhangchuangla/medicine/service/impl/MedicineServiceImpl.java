package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.common.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.mapper.MedicineMapper;
import cn.zhangchuangla.medicine.model.entity.Medicine;
import cn.zhangchuangla.medicine.model.entity.MedicineCategory;
import cn.zhangchuangla.medicine.model.entity.MedicineImage;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineUpdateRequest;
import cn.zhangchuangla.medicine.service.MedicineCategoryService;
import cn.zhangchuangla.medicine.service.MedicineImageService;
import cn.zhangchuangla.medicine.service.MedicineService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 药品服务实现类
 *
 * @author Chuang
 * created on 2025/9/22 13:35
 */
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine> implements MedicineService {

    private final MedicineCategoryService medicineCategoryService;
    private final MedicineImageService medicineImageService;

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
     * @param name      药品名称
     * @param excludeId 排除的药品ID（用于更新时排除自身）
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
    @Transactional(rollbackFor = Exception.class)
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

        // 保存药品
        boolean result = save(medicine);

        // 保存药品图片
        if (result && request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            saveMedicineImages(medicine.getId(), request.getImageUrls());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        // 更新药品基本信息
        boolean result = updateById(medicine);

        // 更新药品图片
        if (request.getImageUrls() != null) {
            updateMedicineImages(request.getId(), request.getImageUrls());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMedicine(List<Long> ids) {
        Assert.notNull(ids, "药品ID列表不能为空");
        Assert.notEmpty(ids, "药品ID列表不能为空");

        // 检查药品是否存在并删除相关图片
        for (Long id : ids) {
            Medicine medicine = getById(id);
            Assert.notNull(medicine, "ID为 " + id + " 的药品不存在");

            // 删除药品相关图片
            medicineImageService.deleteImagesByMedicineId(id);
        }

        return removeByIds(ids);
    }

    @Override
    public List<MedicineImage> getImagesByMedicineId(Long medicineId) {
        return medicineImageService.getImagesByMedicineId(medicineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMedicineImages(Long medicineId, List<String> imageUrls) {
        Assert.notNull(medicineId, "药品ID不能为空");
        Assert.notNull(imageUrls, "图片URL列表不能为空");

        // 检查药品是否存在
        Medicine medicine = getById(medicineId);
        Assert.notNull(medicine, "药品不存在");

        // 删除现有图片
        medicineImageService.deleteImagesByMedicineId(medicineId);

        // 添加新图片
        if (!imageUrls.isEmpty()) {
            saveMedicineImages(medicineId, imageUrls);
        }

        return true;
    }

    /**
     * 保存药品图片
     *
     * @param medicineId 药品ID
     * @param imageUrls  图片URL列表
     */
    private void saveMedicineImages(Long medicineId, List<String> imageUrls) {
        for (String url : imageUrls) {
            MedicineImage image = new MedicineImage();
            image.setMedicineId(medicineId);
            image.setUrl(url);
            image.setSort(imageUrls.indexOf(url));
            image.setCreateTime(new Date());
            medicineImageService.save(image);
        }
    }

}
