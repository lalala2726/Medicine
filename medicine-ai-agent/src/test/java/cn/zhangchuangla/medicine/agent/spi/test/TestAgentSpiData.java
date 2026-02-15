package cn.zhangchuangla.medicine.agent.spi.test;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.analytics.*;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 测试用 SPI 数据容器。
 */
public final class TestAgentSpiData {

    public static UserVo adminUser;
    public static UserVo clientUser;

    public static Page<MallProductDetailDto> productPage;
    public static List<AdminAgentProductDetailVo> productDetails;
    public static List<AdminAgentDrugDetailVo> drugDetails;

    public static Page<OrderWithProductDto> orderPage;
    public static List<AdminOrderDetailVo> orderDetails;

    // Analytics 相关数据
    public static List<OrderTrendPoint> orderTrendPoints;
    public static List<StatusDistribution> statusDistributions;
    public static List<PaymentDistribution> paymentDistributions;
    public static List<HotProductRank> hotProductRanks;
    public static List<ReturnRateStat> returnRateStats;

    public static Long capturedAdminUserId;
    public static Long capturedClientUserId;
    public static MallProductListQueryRequest capturedProductListRequest;
    public static List<Long> capturedProductDetailIds;
    public static List<Long> capturedDrugDetailIds;
    public static AdminMallOrderListRequest capturedOrderListRequest;
    public static List<Long> capturedOrderDetailIds;

    // Analytics 相关捕获变量
    public static boolean capturedAnalyticsOverview;
    public static String capturedAnalyticsPeriod;
    public static boolean capturedAnalyticsStatusDistribution;
    public static boolean capturedAnalyticsPaymentDistribution;
    public static int capturedAnalyticsHotProductsLimit;
    public static int capturedAnalyticsReturnRatesLimit;

    private TestAgentSpiData() {
    }

    public static void reset() {
        adminUser = null;
        clientUser = null;
        productPage = null;
        productDetails = null;
        drugDetails = null;
        orderPage = null;
        orderDetails = null;
        orderTrendPoints = null;
        statusDistributions = null;
        paymentDistributions = null;
        hotProductRanks = null;
        returnRateStats = null;
        capturedAdminUserId = null;
        capturedClientUserId = null;
        capturedProductListRequest = null;
        capturedProductDetailIds = null;
        capturedDrugDetailIds = null;
        capturedOrderListRequest = null;
        capturedOrderDetailIds = null;
        capturedAnalyticsOverview = false;
        capturedAnalyticsPeriod = null;
        capturedAnalyticsStatusDistribution = false;
        capturedAnalyticsPaymentDistribution = false;
        capturedAnalyticsHotProductsLimit = 0;
        capturedAnalyticsReturnRatesLimit = 0;
    }
}
