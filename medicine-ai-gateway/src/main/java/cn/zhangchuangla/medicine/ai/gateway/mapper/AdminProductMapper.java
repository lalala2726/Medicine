package cn.zhangchuangla.medicine.ai.gateway.mapper;

import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.graphql.GraphQLProductQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * AI 网关商品 Mapper
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
public interface AdminProductMapper {

    /**
     * 分页查询商品。
     *
     * @param page 分页参数
     * @return 商品分页列表
     */
    Page<MallProduct> listProductPage(Page<MallProduct> page,
                                      @Param("query") GraphQLProductQuery query);

    /**
     * 根据商品ID查询商品详情。
     *
     * @param id 商品ID
     * @return 商品详情
     */
    MallProduct getProductById(@Param("id") Long id);
}
