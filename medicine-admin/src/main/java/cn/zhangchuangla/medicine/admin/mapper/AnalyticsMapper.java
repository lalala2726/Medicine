package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.vo.analytics.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运营分析专用 Mapper，SQL 全部放在 XML 中。
 */
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
