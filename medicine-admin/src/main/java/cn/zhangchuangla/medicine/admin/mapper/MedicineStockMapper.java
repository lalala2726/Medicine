package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.dto.MedicineStockDto;
import cn.zhangchuangla.medicine.admin.model.entity.MedicineStock;
import cn.zhangchuangla.medicine.admin.model.request.medicine.MedicineStockQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 药品库存数据访问层
 *
 * @author Chuang
 * created on 2025/9/22 15:55
 */
@Mapper
public interface MedicineStockMapper extends BaseMapper<MedicineStock> {

    /**
     * 分页查询药品库存列表
     *
     * @param page    分页对象
     * @param request 查询参数
     * @return 药品库存分页列表
     */
    Page<MedicineStockDto> listMedicineStock(Page<MedicineStock> page, @Param("request") MedicineStockQueryRequest request);

    /**
     * 根据ID获取药品库存详情
     *
     * @param id 库存ID
     * @return 药品库存详情
     */
    MedicineStockDto getMedicineStockById(@Param("id") Long id);
}




