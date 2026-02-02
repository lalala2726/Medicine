package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.service.AgentToolsService;
import cn.zhangchuangla.medicine.admin.service.MallProductService;
import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.AgentSearchRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AI Agent 工具服务实现
 * 为外部 AI Agent 提供统一的数据查询接口
 *
 * @author Chuang
 * <p>
 * created on 2026/2/1
 */
@Service
@AllArgsConstructor
public class AgentToolsServiceImpl implements AgentToolsService, BaseService {

    private final UserService userService;
    private final MallProductService mallProductService;

    @Override
    public User getCurrentUser() {
        Long userId = getUserId();
        return userService.getUserById(userId);
    }

    @Override
    public Page<MallProductDetailDto> searchProducts(AgentSearchRequest request) {
        MallProductListQueryRequest mallProductListQueryRequest = new MallProductListQueryRequest();
        mallProductListQueryRequest.setName(request.getKeyword());
        return mallProductService.listMallProductWithCategory(mallProductListQueryRequest);
    }

    @Override
    public MallProductDetailDto getProductDetail(Long productId) {
        return mallProductService.getMallProductById(productId);
    }
}
