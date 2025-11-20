package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.client.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.client.service.MallProductImageService;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.dto.MallProductWithImageDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.vo.mall.RecommendListVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MallProductServiceImpl extends ServiceImpl<MallProductMapper, MallProduct>
        implements MallProductService {

    private final MallProductMapper mallProductMapper;
    private final MallProductImageService mallProductImageService;

    @Override
    public List<RecommendListVo> recommend() {

        // todo 这边是一个简单推荐,后续推荐的话需要根据销量,商品排序进行推荐
        // 获取状态为启用的商品列表
        List<MallProduct> mallProducts = lambdaQuery()
                .eq(MallProduct::getStatus, 1)
                .list();

        // 如果没有商品，直接返回空列表
        if (mallProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取商品ID列表
        List<Long> productIds = mallProducts.stream()
                .map(MallProduct::getId)
                .toList();

        // 查询商品封面图片
        List<MallProductImage> productCoverImages = mallProductImageService.getProductCoverImage(productIds);

        // 将图片列表转换为Map，方便后续查询
        HashMap<Long, String> imageMap = new HashMap<>();
        productCoverImages.forEach(productImage ->
                imageMap.put(productImage.getProductId(), productImage.getImageUrl()));

        // 转换为推荐列表VO
        return mallProducts.stream().map(product ->
                RecommendListVo.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .cover(imageMap.get(product.getId()))
                        .price(product.getPrice())
                        .salesVolume(product.getSalesVolume() != null ? product.getSalesVolume() : 0)
                        .build()
        ).toList();
    }

    @Override
    public MallProduct getMallProductById(Long id) {
        MallProduct mallProduct = getById(id);
        if (mallProduct == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }
        return mallProduct;
    }

    @Override
    public cn.zhangchuangla.medicine.client.model.vo.MallProductVo getMallProductDetail(Long id) {
        if (id == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "商品ID不能为空");
        }

        // 查询商品详情（包含图片和药品详情）
        MallProductWithImageDto productWithImages = mallProductMapper.getProductWithImagesById(id);
        if (productWithImages == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }

        // 构建返回VO
        cn.zhangchuangla.medicine.client.model.vo.MallProductVo productVo =
                new cn.zhangchuangla.medicine.client.model.vo.MallProductVo();
        productVo.setId(productWithImages.getId());
        productVo.setName(productWithImages.getName());
        productVo.setUnit(productWithImages.getUnit());
        productVo.setPrice(productWithImages.getPrice());
        productVo.setSalesVolume(productWithImages.getSalesVolume());
        productVo.setStock(productWithImages.getStock());
        productVo.setMedicineDetail(productWithImages.getMedicineDetail());

        // 提取图片URL列表
        if (productWithImages.getProductImages() != null && !productWithImages.getProductImages().isEmpty()) {
            List<String> imageUrls = productWithImages.getProductImages().stream()
                    .map(MallProductImage::getImageUrl)
                    .toList();
            productVo.setImages(imageUrls);
        }

        return productVo;
    }

    @Override
    public MallProductWithImageDto getProductWithImagesById(Long id) {
        return mallProductMapper.getProductWithImagesById(id);
    }


    @Override
    public void recordView(Long productId) {
        Objects.requireNonNull(productId);
        // todo 添加商品浏览量
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
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品不存在");
        }
        // 2. 校验库存
        Integer stock = product.getStock();
        if (stock == null || stock < quantity) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
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
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "库存更新失败，请重试");
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
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品不存在");
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
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "库存更新失败，请重试");
        }
    }
}
