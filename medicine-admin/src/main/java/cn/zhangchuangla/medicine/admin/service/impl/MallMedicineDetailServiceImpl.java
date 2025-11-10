package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallMedicineDetailMapper;
import cn.zhangchuangla.medicine.admin.service.MallMedicineDetailService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.entity.MallMedicineDetail;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 药品详情服务实现类
 *
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MallMedicineDetailServiceImpl extends ServiceImpl<MallMedicineDetailMapper, MallMedicineDetail>
        implements MallMedicineDetailService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMedicineDetail(MallMedicineDetail mallMedicineDetail) {
        if (mallMedicineDetail == null) {
            throw new ServiceException("药品详情不能为空");
        }
        if (mallMedicineDetail.getProductId() == null) {
            throw new ServiceException("商品ID不能为空");
        }

        // 检查是否已存在该商品的药品详情
        MallMedicineDetail existingDetail = lambdaQuery()
                .eq(MallMedicineDetail::getProductId, mallMedicineDetail.getProductId())
                .one();

        if (existingDetail != null) {
            // 如果已存在，更新现有记录
            mallMedicineDetail.setId(existingDetail.getId());
            mallMedicineDetail.setUpdateTime(new Date());
            return updateById(mallMedicineDetail);
        }

        // 不存在则新增
        mallMedicineDetail.setCreateTime(new Date());
        return save(mallMedicineDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMedicineDetail(MallMedicineDetail mallMedicineDetail) {
        if (mallMedicineDetail == null) {
            throw new ServiceException("药品详情不能为空");
        }
        if (mallMedicineDetail.getProductId() == null) {
            throw new ServiceException("商品ID不能为空");
        }

        // 查询现有的药品详情
        MallMedicineDetail existingDetail = lambdaQuery()
                .eq(MallMedicineDetail::getProductId, mallMedicineDetail.getProductId())
                .one();

        if (existingDetail == null) {
            // 不存在则新增
            return addMedicineDetail(mallMedicineDetail);
        }

        // 更新记录
        mallMedicineDetail.setId(existingDetail.getId());
        mallMedicineDetail.setUpdateTime(new Date());
        return updateById(mallMedicineDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMedicineDetailByProductId(Long productId) {
        if (productId == null) {
            throw new ServiceException("商品ID不能为空");
        }

        return lambdaUpdate()
                .eq(MallMedicineDetail::getProductId, productId)
                .remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMedicineDetailByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return true;
        }

        return lambdaUpdate()
                .in(MallMedicineDetail::getProductId, productIds)
                .remove();
    }
}
