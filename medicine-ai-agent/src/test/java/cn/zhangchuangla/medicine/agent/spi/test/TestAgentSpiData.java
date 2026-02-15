package cn.zhangchuangla.medicine.agent.spi.test;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
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

    public static Long capturedAdminUserId;
    public static Long capturedClientUserId;
    public static MallProductListQueryRequest capturedProductListRequest;
    public static List<Long> capturedProductDetailIds;
    public static List<Long> capturedDrugDetailIds;
    public static AdminMallOrderListRequest capturedOrderListRequest;
    public static List<Long> capturedOrderDetailIds;

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
        capturedAdminUserId = null;
        capturedClientUserId = null;
        capturedProductListRequest = null;
        capturedProductDetailIds = null;
        capturedDrugDetailIds = null;
        capturedOrderListRequest = null;
        capturedOrderDetailIds = null;
    }
}
