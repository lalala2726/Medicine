package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.medicine.SupplierAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.medicine.SupplierListQueryRequest;
import cn.zhangchuangla.medicine.admin.model.request.medicine.SupplierUpdateRequest;
import cn.zhangchuangla.medicine.common.core.base.Option;
import cn.zhangchuangla.medicine.model.entity.MedicineSupplier;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface MedicineSupplierService extends IService<MedicineSupplier> {

    /**
     * 分页查询供应商列表
     */
    Page<MedicineSupplier> listSupplier(SupplierListQueryRequest request);

    /**
     * 根据ID获取供应商详情
     */
    MedicineSupplier getSupplierById(Long id);

    /**
     * 添加供应商
     */
    boolean addSupplier(SupplierAddRequest request);

    /**
     * 更新供应商
     */
    boolean updateSupplier(SupplierUpdateRequest request);

    /**
     * 删除供应商
     */
    boolean deleteSupplier(List<Long> ids);

    /**
     * 获取供应商下拉选项
     */
    List<Option<Long>> option();

}
