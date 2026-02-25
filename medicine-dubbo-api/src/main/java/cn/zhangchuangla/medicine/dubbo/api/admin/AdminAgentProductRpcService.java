package cn.zhangchuangla.medicine.dubbo.api.admin;

import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.dubbo.api.model.AgentDrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;

import java.util.List;

/**
 * 管理端 Agent 商品只读 RPC。
 */
public interface AdminAgentProductRpcService {

    PageResult<MallProductDetailDto> listProducts(MallProductListQueryRequest query);

    List<MallProductDetailDto> getProductDetailsByIds(List<Long> productIds);

    List<AgentDrugDetailDto> getDrugDetailsByProductIds(List<Long> productIds);
}
