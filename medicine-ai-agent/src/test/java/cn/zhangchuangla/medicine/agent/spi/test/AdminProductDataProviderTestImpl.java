package cn.zhangchuangla.medicine.agent.spi.test;

import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.agent.spi.AdminProductDataProvider;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * AdminProductDataProvider 测试实现。
 */
public class AdminProductDataProviderTestImpl implements AdminProductDataProvider {

    @Override
    public Page<MallProductDetailDto> listProducts(MallProductListQueryRequest request) {
        TestAgentSpiData.capturedProductListRequest = request;
        return TestAgentSpiData.productPage;
    }

    @Override
    public List<AdminAgentProductDetailVo> getProductDetail(List<Long> productIds) {
        TestAgentSpiData.capturedProductDetailIds = productIds;
        return TestAgentSpiData.productDetails;
    }

    @Override
    public List<AdminAgentDrugDetailVo> getDrugDetail(List<Long> productIds) {
        TestAgentSpiData.capturedDrugDetailIds = productIds;
        return TestAgentSpiData.drugDetails;
    }
}
