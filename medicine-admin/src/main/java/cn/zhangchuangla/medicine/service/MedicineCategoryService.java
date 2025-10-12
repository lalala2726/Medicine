package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.common.base.Option;
import cn.zhangchuangla.medicine.model.entity.MedicineCategory;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryListQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.medicine.MedicineCategoryTree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 药品分类服务接口
 *
 * @author Chuang
 * created on 2025/9/21 19:45
 */
public interface MedicineCategoryService extends IService<MedicineCategory> {

    /**
     * 分页查询药品分类列表
     *
     * @param request 查询参数
     * @return 药品分类分页列表
     */
    Page<MedicineCategory> listMedicineCategory(MedicineCategoryListQueryRequest request);

    /**
     * 根据ID获取药品分类详情
     *
     * @param id 分类ID
     * @return 药品分类详情
     */
    MedicineCategory getCategoryById(Long id);

    /**
     * 添加药品分类
     *
     * @param request 添加参数
     * @return 是否成功
     */
    boolean addCategory(MedicineCategoryAddRequest request);

    /**
     * 修改药品分类
     *
     * @param request 修改参数
     * @return 是否成功
     */
    boolean updateCategory(MedicineCategoryUpdateRequest request);

    /**
     * 删除药品分类
     *
     * @param ids 分类ID列表
     * @return 是否成功
     */
    boolean deleteCategory(List<Long> ids);

    /**
     * 获取药品分类树形结构
     *
     * @return 分类树形结构
     */
    List<Option<Long>> option();

    /**
     * 获取药品分类树形结构
     *
     * @return 分类树形结构
     */
    List<MedicineCategoryTree> tree();

}
