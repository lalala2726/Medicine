package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.medicine.SupplierListQueryRequest;
import cn.zhangchuangla.medicine.model.entity.MedicineSupplier;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface SupplierMapper extends BaseMapper<MedicineSupplier> {

    /**
     * 分页查询供应商列表
     *
     * @param page    分页对象
     * @param request 查询参数
     * @return 供应商分页列表
     */
    Page<MedicineSupplier> listSupplier(Page<MedicineSupplier> page, @Param("request") SupplierListQueryRequest request);

}




