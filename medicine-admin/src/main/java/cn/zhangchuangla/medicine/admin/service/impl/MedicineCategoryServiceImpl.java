package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.common.core.base.Option;
import cn.zhangchuangla.medicine.admin.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.admin.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.admin.common.core.utils.Assert;
import cn.zhangchuangla.medicine.admin.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.admin.common.security.base.BaseService;
import cn.zhangchuangla.medicine.admin.mapper.MedicineCategoryMapper;
import cn.zhangchuangla.medicine.admin.mapper.MedicineMapper;
import cn.zhangchuangla.medicine.admin.model.entity.Medicine;
import cn.zhangchuangla.medicine.admin.model.entity.MedicineCategory;
import cn.zhangchuangla.medicine.admin.model.request.medicine.MedicineCategoryAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.medicine.MedicineCategoryListQueryRequest;
import cn.zhangchuangla.medicine.admin.model.request.medicine.MedicineCategoryUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.medicine.MedicineCategoryTree;
import cn.zhangchuangla.medicine.admin.service.MedicineCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MedicineCategoryServiceImpl extends ServiceImpl<MedicineCategoryMapper, MedicineCategory>
        implements MedicineCategoryService, BaseService {

    private final MedicineMapper medicineMapper;

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

        // 如果存在子分类，则不允许删除
        for (Long id : ids) {
            if (lambdaQuery()
                    .eq(MedicineCategory::getParentId, id)
                    .count() > 0) {
                return false;
            }
        }

        long count = medicineMapper.selectCount(
                new LambdaQueryWrapper<Medicine>().in(Medicine::getCategoryId, ids)
        );
        if (count > 0) {
            throw new ServiceException(ResponseResultCode.DELETE_ERROR, "该分类已被药品选择为药品分类，请先解除关联关系");
        }

        return removeByIds(ids);
    }

    /**
     * 获取药品分类树形结构
     *
     * @return 树形结构
     */
    @Override
    public List<Option<Long>> option() {
        // 获取所有未删除的分类
        List<MedicineCategory> allCategories = lambdaQuery()
                .orderByAsc(MedicineCategory::getSort)
                .list();

        // 构建树形结构
        return buildOption(allCategories, 0L);
    }

    /**
     * 获取药品分类树形结构
     *
     * @return 树形结构
     */
    @Override
    public List<MedicineCategoryTree> tree() {
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
     * @param parentId   父分类ID
     * @return 树形结构
     */
    private List<Option<Long>> buildOption(List<MedicineCategory> categories, Long parentId) {
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
                    List<Option<Long>> children = buildOption(categories, category.getId());
                    if (!children.isEmpty()) {
                        option.setChildren(children);
                    }

                    return option;
                })
                .collect(Collectors.toList());
    }

    /**
     * 递归构建树形结构
     *
     * @param categories 所有分类列表
     * @param parentId   父分类ID
     * @return 树形结构
     */
    private List<MedicineCategoryTree> buildTree(List<MedicineCategory> categories, Long parentId) {
        return categories.stream()
                .filter(category -> category.getParentId().equals(parentId))
                .sorted((c1, c2) -> {
                    int sort1 = c1.getSort() != null ? c1.getSort() : 0;
                    int sort2 = c2.getSort() != null ? c2.getSort() : 0;
                    return Integer.compare(sort1, sort2);
                })
                .map(category -> {
                    MedicineCategoryTree tree = new MedicineCategoryTree();
                    tree.setId(category.getId());
                    tree.setName(category.getName());
                    tree.setParentId(category.getParentId());
                    tree.setDescription(category.getDescription());

                    // 递归查找子分类
                    List<MedicineCategoryTree> children = buildTree(categories, category.getId());
                    if (!children.isEmpty()) {
                        tree.setChildren(children);
                    }

                    return tree;
                })
                .collect(Collectors.toList());
    }

}




