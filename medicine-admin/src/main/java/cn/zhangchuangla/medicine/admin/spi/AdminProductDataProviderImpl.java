package cn.zhangchuangla.medicine.admin.spi;

import cn.zhangchuangla.medicine.admin.model.vo.AgentDrugDetailVo;
import cn.zhangchuangla.medicine.admin.service.MallProductService;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.agent.spi.AdminProductDataProvider;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.core.utils.SpringUtils;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Admin 商品数据 SPI 实现。
 */
public class AdminProductDataProviderImpl implements AdminProductDataProvider {

    @Override
    public Page<MallProductDetailDto> listProducts(MallProductListQueryRequest request) {
        MallProductService mallProductService = SpringUtils.getBean(MallProductService.class);
        return mallProductService.listMallProductWithCategory(request);
    }

    @Override
    public List<AdminAgentProductDetailVo> getProductDetail(List<Long> productIds) {
        MallProductService mallProductService = SpringUtils.getBean(MallProductService.class);
        List<MallProductDetailDto> products = mallProductService.getMallProductByIds(productIds);
        return BeanCotyUtils.copyListProperties(products, AdminAgentProductDetailVo.class);
    }

    @Override
    public List<AdminAgentDrugDetailVo> getDrugDetail(List<Long> productIds) {
        MallProductService mallProductService = SpringUtils.getBean(MallProductService.class);
        List<AgentDrugDetailVo> drugDetails = mallProductService.getDrugDetailByProductIds(productIds);
        return BeanCotyUtils.copyListProperties(drugDetails, AdminAgentDrugDetailVo.class);
    }
}
