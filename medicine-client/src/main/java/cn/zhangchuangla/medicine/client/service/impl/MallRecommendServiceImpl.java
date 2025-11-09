package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.service.MallProductImageService;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.client.service.MallRecommendService;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.request.mall.product.RecommendRequest;
import cn.zhangchuangla.medicine.model.vo.RecommendListVo;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 商品推荐占位实现，后续可扩展真实策略
 */
@Service
public class MallRecommendServiceImpl implements MallRecommendService, BaseService {

    private final MallProductService mallProductService;
    private final MallProductImageService mallProductImageService;

    public MallRecommendServiceImpl(MallProductService mallProductService, MallProductImageService mallProductImageService) {
        this.mallProductService = mallProductService;
        this.mallProductImageService = mallProductImageService;
    }

    @Override
    public List<RecommendListVo> recommend(RecommendRequest request) {

        // todo 这边是一个简单推荐,后续推荐的话需要根据销量,商品排序进行推荐
        // 获取状态为启用的商品列表
        List<MallProduct> mallProducts = mallProductService.lambdaQuery()
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
}
