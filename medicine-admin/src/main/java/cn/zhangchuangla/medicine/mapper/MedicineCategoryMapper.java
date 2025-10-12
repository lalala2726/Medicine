package cn.zhangchuangla.medicine.mapper;

import cn.zhangchuangla.medicine.model.entity.MedicineCategory;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineCategoryListQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 药品分类数据访问层
 *
 * @author Chuang
 * created on 2025/9/21 19:45
 */
@Mapper
public interface MedicineCategoryMapper extends BaseMapper<MedicineCategory> {

    /**
     * 分页查询药品分类列表
     *
     * @param page    分页对象
     * @param request 查询参数
     * @return 药品分类分页列表
     */
    Page<MedicineCategory> listMedicineCategory(Page<MedicineCategory> page, @Param("request") MedicineCategoryListQueryRequest request);

}




