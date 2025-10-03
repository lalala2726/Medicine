package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.model.entity.MallCategory;
import cn.zhangchuangla.medicine.model.vo.mall.category.MallCategoryTree;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface MallCategoryService extends IService<MallCategory> {

    /**
     * 商品分类树
     *
     * @return 商品分类树
     */
    List<MallCategoryTree> categoryTree();

}
