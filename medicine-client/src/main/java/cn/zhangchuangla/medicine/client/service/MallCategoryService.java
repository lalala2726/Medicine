package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.MallCategory;
import cn.zhangchuangla.medicine.model.vo.mall.MallCategoryTree;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商城商品分类服务接口（客户端）
 *
 * @author Chuang
 */
public interface MallCategoryService extends IService<MallCategory> {

    /**
     * 获取启用的商品分类树
     *
     * @return 分类树
     */
    List<MallCategoryTree> categoryTree();
}
