package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.model.vo.analytics.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AnalyticsMapper {

    Long countUsers();

    Long countOrders();

    OrderAmountStats orderAmountStats();

    RefundStats refundStats();

    List<OrderTrendPoint> orderTrend(@Param("startTime") LocalDateTime startTime,
                                     @Param("groupFormat") String groupFormat);

    List<StatusDistribution> orderStatusDistribution();

    List<PaymentDistribution> paymentDistribution();

    List<HotProductRank> hotProducts(@Param("limit") int limit);

    List<ReturnRateStat> productReturnRates(@Param("limit") int limit);
}
