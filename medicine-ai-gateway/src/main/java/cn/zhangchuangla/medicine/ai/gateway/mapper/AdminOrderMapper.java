package cn.zhangchuangla.medicine.ai.gateway.mapper;

import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.request.graphql.GraphQLOrderQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * AI 网关订单 Mapper
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
public interface AdminOrderMapper {

    /**
     * 分页查询订单。
     *
     * @param page    分页参数
     * @return 订单分页列表
     */
    Page<MallOrder> listOrderPage(Page<MallOrder> page,
                                  @Param("query") GraphQLOrderQuery query);

    /**
     * 根据订单ID查询订单详情。
     *
     * @param id 订单ID
     * @return 订单详情
     */
    MallOrder getOrderById(@Param("id") Long id);
}
