package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.product.RecommendRequest;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16 15:34
 */
public interface MallRecommendService {

    /**
     * 推荐商品
     *
     * @param request 推荐参数
     * @return 推荐结果
     */
    List<MallProduct> recommend(RecommendRequest request);
}
