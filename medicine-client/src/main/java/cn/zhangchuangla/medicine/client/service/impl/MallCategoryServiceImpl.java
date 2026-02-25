package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallCategoryMapper;
import cn.zhangchuangla.medicine.client.service.MallCategoryService;
import cn.zhangchuangla.medicine.model.entity.MallCategory;
import cn.zhangchuangla.medicine.model.vo.MallCategoryTree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 商城商品分类服务实现类（客户端）
 *
 * @author Chuang
 */
@Service
public class MallCategoryServiceImpl extends ServiceImpl<MallCategoryMapper, MallCategory>
        implements MallCategoryService {

    @Override
    public List<MallCategoryTree> categoryTree() {
        LambdaQueryWrapper<MallCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallCategory::getStatus, 0)
                .orderByAsc(MallCategory::getSort)
                .orderByAsc(MallCategory::getId);

        List<MallCategory> categories = list(queryWrapper);
        if (categories.isEmpty()) {
            return List.of();
        }
        return buildTree(categories, 0L);
    }

    @Override
    public List<MallCategoryTree> categoryChildren(Long parentId) {
        LambdaQueryWrapper<MallCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallCategory::getStatus, 0)
                .orderByAsc(MallCategory::getSort)
                .orderByAsc(MallCategory::getId);

        List<MallCategory> categories = list(queryWrapper);
        if (categories.isEmpty()) {
            return List.of();
        }
        return buildTree(categories, parentId);
    }

    @Override
    public List<MallCategoryTree> categorySiblings(Long parentId) {
        LambdaQueryWrapper<MallCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallCategory::getStatus, 0)
                .eq(MallCategory::getParentId, parentId)
                .orderByAsc(MallCategory::getSort)
                .orderByAsc(MallCategory::getId);

        List<MallCategory> categories = list(queryWrapper);
        if (categories.isEmpty()) {
            return List.of();
        }
        return categories.stream()
                .map(this::toTreeNode)
                .toList();
    }

    private List<MallCategoryTree> buildTree(List<MallCategory> categories, Long parentId) {
        Comparator<MallCategory> comparator = Comparator
                .comparing(MallCategory::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MallCategory::getId, Comparator.nullsLast(Long::compareTo));

        return categories.stream()
                .filter(category -> Objects.equals(category.getParentId(), parentId))
                .sorted(comparator)
                .map(category -> {
                    MallCategoryTree tree = toTreeNode(category);
                    List<MallCategoryTree> children = buildTree(categories, category.getId());
                    if (!children.isEmpty()) {
                        tree.setChildren(children);
                    }
                    return tree;
                })
                .toList();
    }

    private MallCategoryTree toTreeNode(MallCategory category) {
        MallCategoryTree tree = new MallCategoryTree();
        tree.setId(category.getId());
        tree.setName(category.getName());
        tree.setParentId(category.getParentId());
        tree.setSort(category.getSort());
        tree.setStatus(category.getStatus());
        if (StringUtils.hasText(category.getDescription())) {
            tree.setDescription(category.getDescription());
        }
        if (StringUtils.hasText(category.getCover())) {
            tree.setCover(category.getCover());
        }
        return tree;
    }
}
