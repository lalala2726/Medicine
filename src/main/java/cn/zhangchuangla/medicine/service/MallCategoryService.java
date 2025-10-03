package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.common.base.Option;
import cn.zhangchuangla.medicine.model.entity.MallCategory;
import cn.zhangchuangla.medicine.model.request.mall.category.MallCategoryAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.category.MallCategoryListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.category.MallCategoryUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.mall.category.MallCategoryTree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商城商品分类服务接口
 * <p>
 * 提供商城商品分类的业务逻辑处理，包括分类的增删改查、
 * 分类树构建、分类选项获取等功能。
 *
 * @author Chuang
 * created on 2025/10/4 01:45
 */
public interface MallCategoryService extends IService<MallCategory> {

    /**
     * 获取商城商品分类列表
     *
     * @param request 查询参数
     * @return 分页的商城商品分类列表
     */
    Page<MallCategory> listMallCategory(MallCategoryListQueryRequest request);

    /**
     * 商品分类树
     *
     * @return 商品分类树
     */
    List<MallCategoryTree> categoryTree();

    /**
     * 获取商品下拉选项
     *
     * @return 商品分类选项列表
     */
    List<Option<Long>> option();

    /**
     * 根据ID获取商城商品分类
     *
     * @param id 分类ID
     * @return 商城商品分类信息
     */
    MallCategory getCategoryById(Long id);

    /**
     * 添加商城商品分类
     *
     * @param request 添加参数
     * @return 添加结果
     */
    boolean addCategory(MallCategoryAddRequest request);

    /**
     * 修改商城商品分类
     *
     * @param request 修改参数
     * @return 修改结果
     */
    boolean updateCategory(MallCategoryUpdateRequest request);

    /**
     * 删除商城商品分类
     *
     * @param ids 分类ID列表
     * @return 删除结果
     */
    boolean deleteCategory(List<Long> ids);

}
