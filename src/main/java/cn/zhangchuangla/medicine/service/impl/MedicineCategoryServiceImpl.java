package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.base.BaseService;
import cn.zhangchuangla.medicine.common.base.Option;
import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.common.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.mapper.MedicineCategoryMapper;
import cn.zhangchuangla.medicine.model.entity.MedicineCategory;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryUpdateRequest;
import cn.zhangchuangla.medicine.service.MedicineCategoryService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药品分类服务实现类
 *
 * @author Chuang
 * created on 2025/9/21 19:45
 */
@Service
public class MedicineCategoryServiceImpl extends ServiceImpl<MedicineCategoryMapper, MedicineCategory>
        implements MedicineCategoryService, BaseService {

    /**
     * 分页查询药品分类列表
     *
     * @param request 查询参数
     * @return 药品分类分页列表
     */
    @Override
    public Page<MedicineCategory> listMedicineCategory(MedicineCategoryListQueryRequest request) {
        Page<MedicineCategory> page = new Page<>(request.getPageNum(), request.getPageSize());
        return baseMapper.listMedicineCategory(page, request);
    }

    /**
     * 根据ID获取药品分类详情
     *
     * @param id 分类ID
     * @return 药品分类详情
     */
    @Override
    public MedicineCategory getCategoryById(Long id) {
        Assert.notNull(id, "分类ID不能为空");
        return getById(id);
    }

    /**
     * 添加药品分类
     *
     * @param request 添加参数
     * @return 是否成功
     */
    @Override
    public boolean addCategory(MedicineCategoryAddRequest request) {
        Assert.notNull(request, "药品分类添加请求对象不能为空");

        // 检查分类名称是否已存在
        LambdaQueryChainWrapper<MedicineCategory> checkWrapper = lambdaQuery()
                .eq(MedicineCategory::getName, request.getName());
        MedicineCategory existingCategory = checkWrapper.one();
        Assert.isNull(existingCategory, "分类名称已存在");

        // 拷贝属性并创建分类
        MedicineCategory category = BeanCotyUtils.copyProperties(request, MedicineCategory.class);
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());

        return save(category);
    }

    /**
     * 修改药品分类
     *
     * @param request 修改参数
     * @return 是否成功
     */
    @Override
    public boolean updateCategory(MedicineCategoryUpdateRequest request) {
        Assert.notNull(request, "药品分类修改请求对象不能为空");
        Assert.notNull(request.getId(), "分类ID不能为空");

        // 检查分类是否存在
        MedicineCategory existingCategory = getById(request.getId());
        Assert.notNull(existingCategory, "分类不存在");

        // 检查分类名称是否与其他分类重复
        if (!existingCategory.getName().equals(request.getName())) {
            LambdaQueryChainWrapper<MedicineCategory> checkWrapper = lambdaQuery()
                    .eq(MedicineCategory::getName, request.getName())
                    .ne(MedicineCategory::getId, request.getId());
            MedicineCategory duplicateCategory = checkWrapper.one();
            Assert.isNull(duplicateCategory, "分类名称已存在");
        }

        // 拷贝属性并更新分类
        MedicineCategory category = BeanCotyUtils.copyProperties(request, MedicineCategory.class);
        category.setUpdateTime(new Date());

        return updateById(category);
    }

    /**
     * 删除药品分类
     *
     * @param ids 分类ID列表
     * @return 是否成功
     */
    @Override
    public boolean deleteCategory(List<Long> ids) {
        Assert.notNull(ids, "分类ID列表不能为空");
        Assert.notEmpty(ids, "分类ID列表不能为空");

        // 检查分类是否存在
        for (Long id : ids) {
            MedicineCategory category = getById(id);
            Assert.notNull(category, "ID为 " + id + " 的分类不存在");
        }

        return removeByIds(ids);
    }

    /**
     * 获取药品分类树形结构
     *
     * @return 树形结构
     */
    @Override
    public List<Option<Long>> tree() {
        // 获取所有未删除的分类
        List<MedicineCategory> allCategories = lambdaQuery()
                .orderByAsc(MedicineCategory::getSort)
                .list();

        // 构建树形结构
        return buildTree(allCategories, 0L);
    }

    /**
     * 递归构建树形结构
     *
     * @param categories 所有分类列表
     * @param parentId 父分类ID
     * @return 树形结构
     */
    private List<Option<Long>> buildTree(List<MedicineCategory> categories, Long parentId) {
        return categories.stream()
                .filter(category -> category.getParentId().equals(parentId))
                .sorted((c1, c2) -> {
                    int sort1 = c1.getSort() != null ? c1.getSort() : 0;
                    int sort2 = c2.getSort() != null ? c2.getSort() : 0;
                    return Integer.compare(sort1, sort2);
                })
                .map(category -> {
                    Option<Long> option = new Option<>();
                    option.setLabel(category.getName());
                    option.setValue(category.getId());

                    // 递归查找子分类
                    List<Option<Long>> children = buildTree(categories, category.getId());
                    if (!children.isEmpty()) {
                        option.setChildren(children);
                    }

                    return option;
                })
                .collect(Collectors.toList());
    }

}




