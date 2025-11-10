package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.MallMedicineDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 药品详情服务接口
 *
 * @author Chuang
 */
public interface MallMedicineDetailService extends IService<MallMedicineDetail> {

    /**
     * 添加药品详情
     *
     * @param mallMedicineDetail 药品详情
     * @return 添加结果
     */
    boolean addMedicineDetail(MallMedicineDetail mallMedicineDetail);

    /**
     * 更新药品详情
     *
     * @param mallMedicineDetail 药品详情
     * @return 更新结果
     */
    boolean updateMedicineDetail(MallMedicineDetail mallMedicineDetail);

    /**
     * 根据商品ID删除药品详情
     *
     * @param productId 商品ID
     * @return 删除结果
     */
    boolean deleteMedicineDetailByProductId(Long productId);

    /**
     * 批量删除药品详情
     *
     * @param productIds 商品ID列表
     * @return 删除结果
     */
    boolean deleteMedicineDetailByProductIds(List<Long> productIds);
}
