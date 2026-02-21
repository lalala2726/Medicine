package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.agent.model.vo.analytics.PaymentDistribution;
import cn.zhangchuangla.medicine.agent.model.vo.analytics.StatusDistribution;
import cn.zhangchuangla.medicine.model.vo.analytics.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运营分析 Mapper 接口。
 * <p>
 * 提供运营数据分析的数据访问操作，包括统计查询和趋势分析。
 *
 * @author Chuang
 */
@Mapper
public interface AnalyticsMapper {

    /**
     * 统计用户总数。
     *
     * @return 用户总数
     */
    Long countUsers();

    /**
     * 统计订单总数。
     *
     * @return 订单总数
     */
    Long countOrders();

    /**
     * 统计订单金额相关数据。
     *
     * @return 订单金额统计
     */
    OrderAmountStats orderAmountStats();

    /**
     * 统计退款相关数据。
     *
     * @return 退款统计
     */
    RefundStats refundStats();

    /**
     * 查询订单趋势数据。
     *
     * @param startTime   统计起始时间
     * @param groupFormat 时间分组格式
     * @return 订单趋势数据点列表
     */
    List<OrderTrendPoint> orderTrend(@Param("startTime") LocalDateTime startTime,
                                     @Param("groupFormat") String groupFormat);

    /**
     * 查询订单状态分布。
     *
     * @return 订单状态分布列表
     */
    List<StatusDistribution> orderStatusDistribution();

    /**
     * 查询支付方式分布。
     *
     * @return 支付方式分布列表
     */
    List<PaymentDistribution> paymentDistribution();

    /**
     * 查询热销商品排行榜。
     *
     * @param limit 返回数量限制
     * @return 热销商品排行列表
     */
    List<HotProductRank> hotProducts(@Param("limit") int limit);

    /**
     * 查询商品退货率统计。
     *
     * @param limit 返回数量限制
     * @return 商品退货率统计列表
     */
    List<ReturnRateStat> productReturnRates(@Param("limit") int limit);
}
