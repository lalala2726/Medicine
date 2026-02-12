package cn.zhangchuangla.medicine.ai.gateway.service;

import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.graphql.GraphQLProductQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * AI 商品查询服务接口
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
public interface AdminProductQueryService {

    /**
     * 分页查询商品列表。
     *
     * @return 分页结果
     */
    Page<MallProduct> searchProducts(GraphQLProductQuery query);

    /**
     * 根据商品ID查询商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    MallProduct getProductById(Long productId);
}
