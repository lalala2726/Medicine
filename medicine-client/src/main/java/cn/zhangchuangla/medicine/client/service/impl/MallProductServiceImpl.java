package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.client.elasticsearch.service.MallProductSearchService;
import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.client.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.client.model.dto.RecommendProductDto;
import cn.zhangchuangla.medicine.client.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.client.model.vo.MallProductSearchVo;
import cn.zhangchuangla.medicine.client.model.vo.MallProductVo;
import cn.zhangchuangla.medicine.client.service.MallOrderItemService;
import cn.zhangchuangla.medicine.client.service.MallProductImageService;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.client.service.MallProductViewHistoryService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductWithImageDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.vo.RecommendListVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MallProductServiceImpl extends ServiceImpl<MallProductMapper, MallProduct>
        implements MallProductService, BaseService {

    private static final int RECOMMEND_LIMIT = 20;
    private final MallProductMapper mallProductMapper;
    private final MallProductImageService mallProductImageService;
    private final MallProductViewHistoryService mallProductViewHistoryService;
    private final MallOrderItemService mallOrderItemService;
    private final MallProductSearchService mallProductSearchService;

    @Override
    public List<RecommendListVo> recommend() {

        // 获取候选集（销量/浏览量靠前），随后在代码层加入权重+随机
        List<RecommendProductDto> candidates = mallProductMapper.listRecommendProducts();

        // 如果没有商品，直接返回空列表
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // 追加销量信息（单独查询，避免多表关联超过 3 张表）
        Map<Long, Integer> salesMap = mallOrderItemService.getCompletedSalesByProductIds(
                candidates.stream()
                        .map(MallProduct::getId)
                        .toList());
        candidates.forEach(product -> product.setSales(Optional.ofNullable(salesMap.get(product.getId())).orElse(0)));

        // 计算权重，加入适度随机，排序后截取
        List<RecommendProductDto> picked = candidates.stream()
                .sorted((a, b) -> Double.compare(weight(b), weight(a)))
                .limit(RECOMMEND_LIMIT)
                .toList();

        // 提取商品ID列表
        List<Long> productIds = picked.stream()
                .map(MallProduct::getId)
                .toList();

        // 查询商品封面图片
        List<MallProductImage> productCoverImages = mallProductImageService.getProductCoverImage(productIds);

        // 将图片列表转换为Map，方便后续查询
        HashMap<Long, String> imageMap = new HashMap<>();
        productCoverImages.forEach(productImage ->
                imageMap.put(productImage.getProductId(), productImage.getImageUrl()));

        // 转换为推荐列表VO
        return picked.stream().map(product ->
                RecommendListVo.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .cover(imageMap.get(product.getId()))
                        .price(product.getPrice())
                        .sales(product.getSales())
                        .build()
        ).toList();
    }

    /**
     * 权重 = 销量/浏览量/排序号权重 * 随机系数，保证热门优先且保留一定随机性。
     */
    private double weight(RecommendProductDto product) {
        double sales = Optional.ofNullable(product.getSales()).orElse(0);
        double views = Optional.ofNullable(product.getViews()).orElse(0L);
        int sort = Optional.ofNullable(product.getSort()).orElse(100);
        double base = sales * 0.6 + views * 0.25 + (100 - Math.min(sort, 100)) * 0.15;
        double randomFactor = 0.8 + Math.random() * 0.4; // 0.8~1.2
        return base * randomFactor;
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
    public MallProductVo getMallProductDetail(Long id) {
        if (id == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "商品ID不能为空");
        }

        // 查询商品详情（包含图片和药品详情）
        MallProductWithImageDto productWithImages = mallProductMapper.getProductWithImagesById(id);
        if (productWithImages == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }

        productWithImages.setSales(mallOrderItemService.getCompletedSalesByProductId(id));

        // 构建返回VO
        cn.zhangchuangla.medicine.client.model.vo.MallProductVo productVo =
                new cn.zhangchuangla.medicine.client.model.vo.MallProductVo();
        productVo.setId(productWithImages.getId());
        productVo.setName(productWithImages.getName());
        productVo.setUnit(productWithImages.getUnit());
        productVo.setPrice(productWithImages.getPrice());
        productVo.setStock(productWithImages.getStock());
        productVo.setSales(productWithImages.getSales());
        productVo.setDrugDetail(productWithImages.getDrugDetail());

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
        if (id == null) {
            return null;
        }
        MallProductWithImageDto product = mallProductMapper.getProductWithImagesById(id);
        if (product == null) {
            return null;
        }
        product.setSales(mallOrderItemService.getCompletedSalesByProductId(id));
        return product;
    }

    @Override
    public PageResult<MallProductSearchVo> search(MallProductSearchRequest request) {
        int safePageNum = Math.max(request.getPageNum(), 1);
        int safePageSize = Math.max(request.getPageSize(), 1);
        if (!hasSearchCondition(request)) {
            return new PageResult<>((long) safePageNum, (long) safePageSize, 0L, Collections.emptyList());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            request.setKeyword(request.getKeyword().trim());
        }
        if (StringUtils.hasText(request.getCategoryName())) {
            request.setCategoryName(request.getCategoryName().trim());
        }
        if (StringUtils.hasText(request.getEfficacy())) {
            request.setEfficacy(request.getEfficacy().trim());
        }
        request.setPageNum(safePageNum);
        request.setPageSize(safePageSize);

        SearchHits<MallProductDocument> hits = mallProductSearchService.search(request);
        if (hits == null || hits.isEmpty()) {
            return new PageResult<>((long) safePageNum, (long) safePageSize, 0L, Collections.emptyList());
        }

        List<MallProductSearchVo> rows = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toSearchVo)
                .filter(vo -> vo.getProductId() != null)
                .toList();

        return new PageResult<>(
                (long) safePageNum,
                (long) safePageSize,
                hits.getTotalHits(),
                rows
        );
    }

    private MallProductSearchVo toSearchVo(MallProductDocument doc) {
        if (doc == null) {
            return MallProductSearchVo.builder().build();
        }

        return MallProductSearchVo.builder()
                .productId(doc.getId())
                .productName(doc.getName())
                .cover(doc.getCoverImage())
                .price(doc.getPrice())
                .build();
    }

    @Override
    public List<String> suggest(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        return mallProductSearchService.suggest(keyword.trim(), 10);
    }

    @Override
    public MallProductDetailDto getProductAndDrugInfoById(Long id) {
        Assert.isPositive(id, "商品ID不能为空");
        MallProductDetailDto mallProductDetailDto = mallProductMapper.getProductAndDrugInfoById(id);
        if (mallProductDetailDto == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }
        mallProductDetailDto.setSales(mallOrderItemService.getCompletedSalesByProductId(id));
        List<String> imageUrls = mallProductImageService.lambdaQuery()
                .eq(MallProductImage::getProductId, id)
                .orderByAsc(MallProductImage::getSort)
                .list()
                .stream()
                .map(MallProductImage::getImageUrl)
                .toList();
        mallProductDetailDto.setImages(imageUrls);
        return mallProductDetailDto;

    }

    @Override
    public void recordView(Long productId) {
        Objects.requireNonNull(productId);
        Long userId = getUserId();
        if (userId == null) {
            return;
        }
        mallProductViewHistoryService.recordViewHistory(userId, productId);
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

    /**
     * 判断是否提供了至少一个搜索条件。
     *
     * @param request 搜索请求
     * @return 是否存在有效搜索条件
     */
    private boolean hasSearchCondition(MallProductSearchRequest request) {
        return StringUtils.hasText(request.getKeyword())
                || StringUtils.hasText(request.getCategoryName())
                || StringUtils.hasText(request.getEfficacy());
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
