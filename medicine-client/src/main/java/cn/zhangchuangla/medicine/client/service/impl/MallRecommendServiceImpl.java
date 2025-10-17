package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.service.MallRecommendService;
import cn.zhangchuangla.medicine.client.service.MallUserBrowseHistoryService;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.product.RecommendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16 15:34
 */
@Service
@RequiredArgsConstructor
public class MallRecommendServiceImpl implements MallRecommendService, BaseService {

    private final MallUserBrowseHistoryService mallUserBrowseHistoryService;

    /**
     * 推荐商品
     * 热度值 = (销量 × 0.7) + (浏览量 × 0.3)
     *
     * @param request 请求参数
     * @return 推荐商品列表
     */
    @Override
    public List<MallProduct> recommend(RecommendRequest request) {
        // 当用户没有登录的时候,只能推荐最大50个商品信息
        return List.of();
    }
}
