package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.mapper.MallCategoryMapper;
import cn.zhangchuangla.medicine.model.entity.MallCategory;
import cn.zhangchuangla.medicine.model.vo.mall.category.MallCategoryTree;
import cn.zhangchuangla.medicine.service.MallCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chuang
 */
@Service
public class MallCategoryServiceImpl extends ServiceImpl<MallCategoryMapper, MallCategory>
        implements MallCategoryService {

    @Override
    public List<MallCategoryTree> categoryTree() {
        List<MallCategory> categories = list();
        if (categories.isEmpty()) {
            return List.of();
        }
        return buildTree(categories, 0L);
    }


    /**
     * 递归构建树形结构
     *
     * @param categories 所有分类列表
     * @param parentId   父分类ID
     * @return 树形结构
     */
    private List<MallCategoryTree> buildTree(List<MallCategory> categories, Long parentId) {
        return categories.stream()
                .filter(category -> category.getParentId().equals(parentId))
                .sorted((c1, c2) -> Integer.compare(c2.getSort(), c1.getSort()))
                .map(category -> {
                    MallCategoryTree tree = new MallCategoryTree();
                    tree.setId(category.getId());
                    tree.setCategoryName(category.getName());
                    tree.setParentId(category.getParentId());
                    tree.setSort(category.getSort());
                    tree.setStatus(category.getStatus());
                    List<MallCategoryTree> mallCategoryTrees = buildTree(categories, category.getId());
                    if (!mallCategoryTrees.isEmpty()) {
                        tree.setChildren(mallCategoryTrees);
                    }
                    return tree;
                }).toList();
    }
}




