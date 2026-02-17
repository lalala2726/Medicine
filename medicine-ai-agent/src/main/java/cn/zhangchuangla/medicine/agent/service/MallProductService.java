package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface MallProductService {

    Page<MallProductDetailDto> listProducts(MallProductListQueryRequest request);

    List<AdminAgentProductDetailVo> getProductDetail(List<Long> productIds);

    List<AdminAgentDrugDetailVo> getDrugDetail(List<Long> productIds);
}
