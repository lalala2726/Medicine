package cn.zhangchuangla.medicine.ai.gateway.service.impl;

import cn.zhangchuangla.medicine.ai.gateway.mapper.AdminProductMapper;
import cn.zhangchuangla.medicine.ai.gateway.service.AdminProductQueryService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.graphql.GraphQLProductQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 商品查询服务实现
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
@Service
public class AdminProductQueryServiceImpl implements AdminProductQueryService, BaseService {

    private final AdminProductMapper adminProductMapper;

    public AdminProductQueryServiceImpl(AdminProductMapper adminProductMapper) {
        this.adminProductMapper = adminProductMapper;
    }

    @Override
    public Page<MallProduct> searchProducts(GraphQLProductQuery query) {
        GraphQLProductQuery safeQuery = query == null ? new GraphQLProductQuery() : query;
        safeQuery.setName(StringUtils.trimToNull(safeQuery.getName()));
        Page<MallProduct> page = adminProductMapper.listProductPage(safeQuery.toPage(), safeQuery);
        if (page.getRecords() == null) {
            page.setRecords(List.of());
        }
        return page;
    }

    @Override
    public MallProduct getProductById(Long productId) {
        if (productId == null || productId <= 0) {
            throw new ServiceException("商品ID不能为空");
        }
        MallProduct product = adminProductMapper.getProductById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        return product;
    }
}
