package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.client.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.entity.MallProductWithImageDto;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MallProductServiceImpl extends ServiceImpl<MallProductMapper, MallProduct>
        implements MallProductService {

    private final MallProductMapper mallProductMapper;

    @Override
    public MallProduct getMallProductById(Long id) {
        MallProduct mallProduct = getById(id);
        if (mallProduct == null) {
            throw new ServiceException(ResponseResultCode.RESULT_IS_NULL, "商品不存在");
        }
        return mallProduct;
    }

    @Override
    public MallProductWithImageDto getProductWithImagesById(Long id) {
        return mallProductMapper.getProductWithImagesById(id);
    }


    @Override
    public void recordView(Long productId) {
        Objects.requireNonNull(productId);
    }

    /**
     * 计算商品浏览量
     *
     * @param views      商品浏览量
     * @param timeMillis 时间戳
     * @return 计算后的商品浏览量
     */
    public Double calculateProductViews(int views, long timeMillis) {
        return views + 1 - timeMillis / 1e13;
    }


    @Override
    public long getViewCount(Long productId, ProductViewPeriod period) {
        return 1L;
    }

    @Override
    @Transactional
    public void deductStock(Long productId, Integer quantity) {
        Assert.isPositive(quantity, "商品数量不能小于0");
        Assert.isPositive(productId, "商品ID不能小于0");
        // 1. 查询商品信息
        MallProduct product = getById(productId);
        if (product == null) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "商品不存在");
        }
        // 2. 校验库存
        Integer stock = product.getStock();
        if (stock == null || stock < quantity) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR,
                    String.format("商品库存不足，当前库存：%d", stock));
        }
        // 3. 扣减库存，带乐观锁防止并发超卖
        int updated = baseMapper.updateStockWithVersion(
                productId,
                quantity,
                product.getVersion()
        );

        if (updated == 0) {
            // 更新失败说明库存不足或版本冲突
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "库存更新失败，请重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreStock(Long productId, Integer quantity) {
        Assert.isPositive(quantity, "商品数量不能小于0");
        Assert.isPositive(productId, "商品ID不能小于0");
        MallProduct mallProduct = lambdaQuery()
                .eq(MallProduct::getId, productId)
                .select(MallProduct::getStock, MallProduct::getVersion)
                .one();

        if (mallProduct == null) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "商品不存在");
        }
        Integer currentStock = mallProduct.getStock();
        int newStock = (currentStock == null ? 0 : currentStock) + quantity;
        int currentVersion = mallProduct.getVersion() == null ? 0 : mallProduct.getVersion();
        boolean updated = lambdaUpdate()
                .eq(MallProduct::getId, productId)
                .eq(MallProduct::getVersion, currentVersion)
                .set(MallProduct::getStock, newStock)
                .set(MallProduct::getVersion, currentVersion + 1)
                .update();
        if (!updated) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "库存更新失败，请重试");
        }
    }
}
