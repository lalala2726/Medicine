package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.client.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisHashCache;
import cn.zhangchuangla.medicine.common.redis.core.RedisZSetCache;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MallProductServiceImpl extends ServiceImpl<MallProductMapper, MallProduct>
        implements MallProductService {

    /**
     * Redis Key 约定：浏览榜、累计浏览次数、热度榜、销量兜底值
     */
    private static final String PRODUCT_VIEW_RANK_KEY = "mall:product:view:rank";
    private static final String PRODUCT_VIEW_COUNT_KEY = "mall:product:view:count";
    private static final String PRODUCT_HEAT_RANK_KEY = "mall:product:heat:rank";
    private static final String PRODUCT_SALES_STUB_KEY = "mall:product:sales:stub";
    private static final String PRODUCT_VIEW_TIMELINE_KEY_PREFIX = "mall:product:view:timeline:";
    private static final double SALES_WEIGHT = 0.7D;
    private static final double VIEW_WEIGHT = 0.3D;
    private static final long SCORE_FACTOR = 1_000_000_000_000L;
    private static final long STUB_SALES_MIN = 10L;
    private static final long STUB_SALES_MAX = 100L;
    private static final long TIMELINE_EXPIRE_DAYS = 90L;
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private final RedisZSetCache redisZSetCache;
    private final RedisHashCache redisHashCache;

    @Override
    @Cacheable(cacheNames = RedisConstants.MallProduct.CACHE_NAME, key = "#id", unless = "#result == null")
    public MallProduct getMallProductById(Long id) {
        MallProduct mallProduct = getById(id);
        if (mallProduct == null) {
            throw new ServiceException(ResponseResultCode.RESULT_IS_NULL, "商品不存在");
        }
        recordView(id);
        return mallProduct;
    }


    @Override
    public void recordView(Long productId) {
        Objects.requireNonNull(productId, "商品ID不能为空");

        MallProduct product = getById(productId);
        if (product == null) {
            throw new ServiceException(ResponseResultCode.RESULT_IS_NULL, "商品不存在");
        }

        long now = System.currentTimeMillis();
        // Hash 里累计浏览次数，方便后续统计，也作为热度计算的原始数据
        long viewCount = redisHashCache.hIncrement(PRODUCT_VIEW_COUNT_KEY, productId.toString(), 1L);
        double viewScore = buildRankScore(viewCount, now);
        // 浏览榜按“次数 + 时间碎片”排序，保证同分下新事件靠前
        redisZSetCache.zAdd(PRODUCT_VIEW_RANK_KEY, productId.toString(), viewScore);
        // 记录时间序列，支持小时/周/月统计
        String timelineKey = buildTimelineKey(productId);
        String timelineMember = productId + ":" + now + ":" + ThreadLocalRandom.current().nextInt(1_000_000);
        redisZSetCache.zAdd(timelineKey, timelineMember, now);
        redisZSetCache.expire(timelineKey, TIMELINE_EXPIRE_DAYS, TimeUnit.DAYS);

        long salesVolume = resolveSalesVolume(product);
        double heatValue = computeHeat(salesVolume, viewCount);
        double heatScore = buildRankScore(heatValue, now);
        // 热度榜综合销量与浏览量，持续复用相同的排序策略
        redisZSetCache.zAdd(PRODUCT_HEAT_RANK_KEY, productId.toString(), heatScore);
    }

    @Override
    public long getViewCount(Long productId, ProductViewPeriod period) {
        Objects.requireNonNull(productId, "商品ID不能为空");
        // 确认商品存在，避免查询无效ID
        MallProduct product = getById(productId);
        if (product == null) {
            throw new ServiceException(ResponseResultCode.RESULT_IS_NULL, "商品不存在");
        }

        ProductViewPeriod targetPeriod = period == null ? ProductViewPeriod.TOTAL : period;
        if (targetPeriod == ProductViewPeriod.TOTAL) {
            Long total = redisHashCache.hGet(PRODUCT_VIEW_COUNT_KEY, productId.toString());
            return total == null ? 0L : total;
        }

        String timelineKey = buildTimelineKey(productId);
        long now = System.currentTimeMillis();
        long start = resolvePeriodStartMillis(targetPeriod);
        if (start >= now) {
            return 0L;
        }
        return redisZSetCache.zCount(timelineKey, start, now);
    }

    private String buildTimelineKey(Long productId) {
        return PRODUCT_VIEW_TIMELINE_KEY_PREFIX + productId;
    }

    /**
     * 解析商品销量：有真实销量直接使用，否则生成并缓存一个兜底随机值，避免热度计算为零
     */
    private long resolveSalesVolume(MallProduct product) {
        if (product.getSalesVolume() != null && product.getSalesVolume() > 0) {
            redisHashCache.hRemove(PRODUCT_SALES_STUB_KEY, product.getId().toString());
            return product.getSalesVolume();
        }

        Long cached = redisHashCache.hGet(PRODUCT_SALES_STUB_KEY, product.getId().toString());
        if (cached != null) {
            return cached;
        }

        long stub = ThreadLocalRandom.current().nextLong(STUB_SALES_MIN, STUB_SALES_MAX + 1);
        redisHashCache.hPut(PRODUCT_SALES_STUB_KEY, product.getId().toString(), stub);
        return stub;
    }

    /**
     * 计算热度：销量 70% + 浏览 30%
     */
    private double computeHeat(long salesVolume, long viewCount) {
        return salesVolume * SALES_WEIGHT + viewCount * VIEW_WEIGHT;
    }

    /**
     * 构造可比较的分数：整位存指标，小数片段塞时间，兼顾排序与实时性
     */
    private double buildRankScore(double baseValue, long timestamp) {
        double timeFraction = (timestamp % SCORE_FACTOR) / (double) SCORE_FACTOR;
        return baseValue * SCORE_FACTOR + timeFraction;
    }

    /**
     * 根据周期计算时间窗口起点（毫秒）
     */
    private long resolvePeriodStartMillis(ProductViewPeriod period) {
        ZonedDateTime now = ZonedDateTime.now(DEFAULT_ZONE);
        ZonedDateTime start;
        switch (period) {
            case HOUR -> start = now.truncatedTo(ChronoUnit.HOURS);
            case DAY -> start = now.truncatedTo(ChronoUnit.DAYS);
            case WEEK -> {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int firstDay = weekFields.getFirstDayOfWeek().getValue();
                DayOfWeek firstDayOfWeek = DayOfWeek.of(firstDay);
                start = now.truncatedTo(ChronoUnit.DAYS)
                        .with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
            }
            case MONTH -> start = now.truncatedTo(ChronoUnit.DAYS)
                    .with(TemporalAdjusters.firstDayOfMonth());
            default -> start = ZonedDateTime.ofInstant(java.time.Instant.EPOCH, DEFAULT_ZONE);
        }
        return start.toInstant().toEpochMilli();
    }

}
