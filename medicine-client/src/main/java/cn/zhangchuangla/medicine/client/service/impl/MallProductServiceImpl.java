package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.client.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisZSetCache;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MallProductServiceImpl extends ServiceImpl<MallProductMapper, MallProduct>
        implements MallProductService {


    private final RedisZSetCache redisZSetCache;

    private final static String REDIS_KEY_PRODUCT_VIEW_COUNT = "product:view:count:";


    @Override
    @Cacheable(cacheNames = RedisConstants.MallProduct.CACHE_NAME, key = "#id", unless = "#result == null")
    public MallProduct getMallProductById(Long id) {
        MallProduct mallProduct = getById(id);
        if (mallProduct == null) {
            throw new ServiceException(ResponseResultCode.RESULT_IS_NULL, "商品不存在");
        }
        return mallProduct;
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
}
