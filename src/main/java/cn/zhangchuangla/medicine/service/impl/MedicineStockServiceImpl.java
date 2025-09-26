package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.base.BaseService;
import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.common.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.mapper.MedicineStockMapper;
import cn.zhangchuangla.medicine.model.dto.MedicineStockDto;
import cn.zhangchuangla.medicine.model.entity.Medicine;
import cn.zhangchuangla.medicine.model.entity.MedicineStock;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineStockAddRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineStockQueryRequest;
import cn.zhangchuangla.medicine.model.request.medicine.MedicineStockUpdateRequest;
import cn.zhangchuangla.medicine.service.MedicineService;
import cn.zhangchuangla.medicine.service.MedicineStockService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 药品库存服务实现类
 *
 * @author Chuang
 * created on 2025/9/22 15:55
 */
@Service
public class MedicineStockServiceImpl extends ServiceImpl<MedicineStockMapper, MedicineStock>
        implements MedicineStockService, BaseService {

    private final MedicineService medicineService;

    public MedicineStockServiceImpl(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    /**
     * 分页查询药品库存列表
     *
     * @param request 查询参数
     * @return 药品库存分页列表
     */
    @Override
    public Page<MedicineStockDto> listMedicineStock(MedicineStockQueryRequest request) {
        Page<MedicineStock> page = new Page<>(request.getPageNum(), request.getPageSize());
        return baseMapper.listMedicineStock(page, request);
    }

    /**
     * 根据ID获取药品库存详情
     *
     * @param id 库存ID
     * @return 药品库存详情
     */
    @Override
    public MedicineStock getMedicineStockById(Long id) {
        Assert.notNull(id, "库存ID不能为空");
        return getById(id);
    }

    /**
     * 添加药品库存
     *
     * @param request 添加参数
     * @return 是否成功
     */
    @Override
    public boolean addMedicineStock(MedicineStockAddRequest request) {
        Assert.notNull(request, "药品库存添加请求对象不能为空");

        // 检查药品是否存在
        Medicine medicine = medicineService.getById(request.getMedicineId());
        Assert.notNull(medicine, "药品不存在");

        // 检查批次号是否已存在
        LambdaQueryChainWrapper<MedicineStock> checkWrapper = lambdaQuery()
                .eq(MedicineStock::getMedicineId, request.getMedicineId())
                .eq(MedicineStock::getBatchNo, request.getBatchNo());
        MedicineStock existingStock = checkWrapper.one();
        Assert.isNull(existingStock, "该药品的批次号已存在");

        // 拷贝属性并创建库存
        MedicineStock stock = BeanCotyUtils.copyProperties(request, MedicineStock.class);
        stock.setCreateTime(new Date());
        stock.setUpdateTime(new Date());

        return save(stock);
    }

    /**
     * 修改药品库存
     *
     * @param request 修改参数
     * @return 是否成功
     */
    @Override
    public boolean updateMedicineStock(MedicineStockUpdateRequest request) {
        Assert.notNull(request, "药品库存修改请求对象不能为空");
        Assert.notNull(request.getId(), "库存ID不能为空");

        // 检查库存是否存在
        MedicineStock existingStock = getById(request.getId());
        Assert.notNull(existingStock, "库存记录不存在");

        // 检查药品是否存在
        Medicine medicine = medicineService.getById(request.getMedicineId());
        Assert.notNull(medicine, "药品不存在");

        // 检查批次号是否与其他记录重复
        if (!existingStock.getBatchNo().equals(request.getBatchNo()) ||
                !existingStock.getMedicineId().equals(request.getMedicineId())) {
            LambdaQueryChainWrapper<MedicineStock> checkWrapper = lambdaQuery()
                    .eq(MedicineStock::getMedicineId, request.getMedicineId())
                    .eq(MedicineStock::getBatchNo, request.getBatchNo())
                    .ne(MedicineStock::getId, request.getId());
            MedicineStock duplicateStock = checkWrapper.one();
            Assert.isNull(duplicateStock, "该药品的批次号已存在");
        }

        // 拷贝属性并更新库存
        MedicineStock stock = BeanCotyUtils.copyProperties(request, MedicineStock.class);
        stock.setUpdateTime(new Date());

        return updateById(stock);
    }

    /**
     * 删除药品库存
     *
     * @param ids 库存ID列表
     * @return 是否成功
     */
    @Override
    public boolean deleteMedicineStock(List<Long> ids) {
        Assert.notNull(ids, "库存ID列表不能为空");
        Assert.notEmpty(ids, "库存ID列表不能为空");

        // 检查库存记录是否存在
        for (Long id : ids) {
            MedicineStock stock = getById(id);
            Assert.notNull(stock, "ID为 " + id + " 的库存记录不存在");
        }

        return removeByIds(ids);
    }

}




