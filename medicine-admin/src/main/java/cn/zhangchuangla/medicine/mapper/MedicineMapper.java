package cn.zhangchuangla.medicine.mapper;

import cn.zhangchuangla.medicine.common.core.model.entity.Medicine;
import cn.zhangchuangla.medicine.common.core.model.request.medicine.MedicineListQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 药品数据访问层
 *
 * @author Chuang
 * created on 2025/9/22 13:35
 */
@Mapper
public interface MedicineMapper extends BaseMapper<Medicine> {

    /**
     * 分页查询药品列表
     *
     * @param page    分页对象
     * @param request 查询参数
     * @return 药品分页列表
     */
    Page<Medicine> listMedicine(Page<Medicine> page, @Param("request") MedicineListQueryRequest request);
}
