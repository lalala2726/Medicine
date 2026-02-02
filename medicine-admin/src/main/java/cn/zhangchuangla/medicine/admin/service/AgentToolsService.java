package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.AgentSearchRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * AI Agent 工具服务接口
 * 为外部 AI Agent 提供统一的数据查询接口
 *
 * @author Chuang
 * <p>
 * created on 2026/2/1
 */
public interface AgentToolsService {

    User getCurrentUser();

    Page<MallProductDetailDto> searchProducts(AgentSearchRequest request);

    MallProductDetailDto getProductDetail(Long productId);
}
