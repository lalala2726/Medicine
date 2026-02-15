package cn.zhangchuangla.medicine.agent.spi;

import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Admin 端智能体商品数据提供者。
 */
public interface AdminProductDataProvider {

    /**
     * 分页查询商品列表。
     */
    Page<MallProductDetailDto> listProducts(MallProductListQueryRequest request);

    /**
     * 批量查询商品详情。
     */
    List<AdminAgentProductDetailVo> getProductDetail(List<Long> productIds);

    /**
     * 批量查询药品详情。
     */
    List<AdminAgentDrugDetailVo> getDrugDetail(List<Long> productIds);
}
