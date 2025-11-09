package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.request.mall.product.RecommendRequest;
import cn.zhangchuangla.medicine.model.vo.RecommendListVo;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16
 */
public interface MallRecommendService {

    /**
     * 推荐商品
     *
     * @param request 推荐参数
     * @return 推荐结果
     */
    List<RecommendListVo> recommend(RecommendRequest request);
}
