package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.client.service.MallRecommendService;
import cn.zhangchuangla.medicine.client.service.MallUserBrowseHistoryService;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.product.RecommendRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16 15:34
 */
@Service
@RequiredArgsConstructor
public class MallRecommendServiceImpl implements MallRecommendService, BaseService {

    /**
     * Redis 热度榜 Key 与默认分页参数
     */
    private static final String PRODUCT_HEAT_RANK_KEY = "mall:product:heat:rank";
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_ANONYMOUS_SIZE = 50;

    private final MallUserBrowseHistoryService mallUserBrowseHistoryService;
    private final MallProductService mallProductService;
    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 推荐商品
     * 热度值 = (销量 × 0.7) + (浏览量 × 0.3)
     *
     * @param request 请求参数
     * @return 推荐商品列表
     */
    @Override
    public List<MallProduct> recommend(RecommendRequest request) {
        int size = normalizeSize(Optional.ofNullable(request).map(RecommendRequest::getSize).orElse(null));
        if (size <= 0) {
            return Collections.emptyList();
        }
        int cursor = Math.max(Optional.ofNullable(request).map(RecommendRequest::getHotCursor).orElse(0), 0);

        List<Long> rankedProductIds = fetchRankedProductIds(cursor, size);
        if (rankedProductIds.isEmpty()) {
            return fallbackRecommend(size, Collections.emptySet());
        }

        Long userId = isLogin() ? getUserId() : null;
        // 维护一个排除集合，避免推荐重复商品
        Set<Long> excluded = new LinkedHashSet<>(rankedProductIds);
        List<MallProduct> rankedProducts = loadRankedProducts(rankedProductIds, userId, size, excluded);

        if (rankedProducts.size() >= size) {
            return rankedProducts;
        }

        List<MallProduct> fallback = fallbackRecommend(size - rankedProducts.size(), excluded);
        if (userId != null && !fallback.isEmpty()) {
            fallback = fallback.stream()
                    .filter(product -> !mallUserBrowseHistoryService.hasBrowsed(userId, product.getId()))
                    .toList();
        }

        rankedProducts.addAll(fallback);
        if (rankedProducts.size() > size) {
            return rankedProducts.subList(0, size);
        }

        return rankedProducts;
    }

    /**
     * 规范化分页数量：默认 10 条，未登录用户最多 50 条
     */
    private int normalizeSize(Integer requestedSize) {
        int size = (requestedSize == null || requestedSize <= 0) ? DEFAULT_SIZE : requestedSize;
        if (!isLogin()) {
            size = Math.min(size, MAX_ANONYMOUS_SIZE);
        }
        return size;
    }

    /**
     * 从 Redis 热度榜取出指定区间的商品 ID
     */
    private List<Long> fetchRankedProductIds(long cursor, int size) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(PRODUCT_HEAT_RANK_KEY, cursor, cursor + size - 1L);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = new ArrayList<>(tuples.size());
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            if (tuple == null || tuple.getValue() == null) {
                continue;
            }
            try {
                productIds.add(Long.parseLong(String.valueOf(tuple.getValue())));
            } catch (NumberFormatException ignored) {
                // 忽略非法的商品ID
            }
        }
        return productIds;
    }

    /**
     * 将热度榜的 ID 转换为实体，并根据登录状态过滤掉已浏览商品
     */
    private List<MallProduct> loadRankedProducts(List<Long> productIds, Long userId, int limit, Set<Long> excluded) {
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<MallProduct> products = mallProductService.listByIds(productIds);
        Map<Long, MallProduct> productMap = products.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MallProduct::getId, product -> product, (a, b) -> a, LinkedHashMap::new));

        List<MallProduct> ordered = new ArrayList<>(limit);
        for (Long productId : productIds) {
            excluded.add(productId);
            MallProduct product = productMap.get(productId);
            if (product == null) {
                continue;
            }
            if (product.getStatus() != null && product.getStatus() == 0) {
                continue;
            }
            if (userId != null && mallUserBrowseHistoryService.hasBrowsed(userId, productId)) {
                continue;
            }
            ordered.add(product);
            if (ordered.size() >= limit) {
                break;
            }
        }
        return ordered;
    }

    /**
     * Redis 榜单不足时的补足策略：从数据库挑选热销在售商品
     */
    private List<MallProduct> fallbackRecommend(int need, Set<Long> excluded) {
        if (need <= 0) {
            return Collections.emptyList();
        }

        Page<MallProduct> page = new Page<>(1, need);
        return mallProductService.lambdaQuery()
                .eq(MallProduct::getStatus, 1)
                .notIn(!excluded.isEmpty(), MallProduct::getId, excluded)
                .orderByDesc(MallProduct::getSalesVolume)
                .orderByDesc(MallProduct::getUpdateTime)
                .page(page)
                .getRecords();
    }
}
