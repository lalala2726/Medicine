package cn.zhangchuangla.medicine.mapper;

import cn.zhangchuangla.medicine.model.entity.Supplier;
import cn.zhangchuangla.medicine.model.request.medicine.SupplierListQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface SupplierMapper extends BaseMapper<Supplier> {

    /**
     * 分页查询供应商列表
     *
     * @param page    分页对象
     * @param request 查询参数
     * @return 供应商分页列表
     */
    Page<Supplier> listSupplier(Page<Supplier> page, @Param("request") SupplierListQueryRequest request);

}




