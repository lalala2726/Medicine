package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.service.MallRecommendService;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.product.RecommendRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 商品推荐占位实现，后续可扩展真实策略
 */
@Service
public class MallRecommendServiceImpl implements MallRecommendService, BaseService {

    @Override
    public List<MallProduct> recommend(RecommendRequest request) {
        return Collections.emptyList();
    }
}
