package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.model.vo.AgentDrugDetailVo;
import cn.zhangchuangla.medicine.admin.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.dubbo.api.admin.AdminAgentProductRpcService;
import cn.zhangchuangla.medicine.dubbo.api.model.AgentDrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 管理端 Agent 商品 RPC Provider。
 */
@DubboService(interfaceClass = AdminAgentProductRpcService.class, group = "medicine-admin", version = "1.0.0")
@RequiredArgsConstructor
public class AdminAgentProductRpcServiceImpl implements AdminAgentProductRpcService {

    private final MallProductService mallProductService;

    @Override
    public PageResult<MallProductDetailDto> listProducts(MallProductListQueryRequest query) {
        MallProductListQueryRequest request = query == null ? new MallProductListQueryRequest() : query;
        Page<MallProductDetailDto> page = mallProductService.listMallProductWithCategory(request);
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public List<MallProductDetailDto> getProductDetailsByIds(List<Long> productIds) {
        return mallProductService.getMallProductByIds(productIds);
    }

    @Override
    public List<AgentDrugDetailDto> getDrugDetailsByProductIds(List<Long> productIds) {
        List<AgentDrugDetailVo> source = mallProductService.getDrugDetailByProductIds(productIds);
        return BeanCotyUtils.copyListProperties(source, AgentDrugDetailDto.class);
    }
}
