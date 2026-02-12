package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.admin.model.vo.MallOrderListVo;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final MallOrderService mallOrderService;
    private final MallOrderItemService mallOrderItemService;

    @Override
    public User getCurrentUser() {
        Long userId = getUserId();
        return userService.getUserById(userId);
    }

    @Override
    public Page<MallProductDetailDto> searchProducts(MallProductListQueryRequest request) {
        return mallProductService.listMallProductWithCategory(request);
    }

    @Override
    public MallProductDetailDto getProductDetail(Long productId) {
        return mallProductService.getMallProductById(productId);
    }

    @Override
    public Page<OrderWithProductDto> getOrderList(MallOrderListRequest request) {
        MallOrderListRequest safeRequest = request == null ? new MallOrderListRequest() : request;
        return mallOrderService.orderWithProduct(safeRequest);
    }

    @Override
    public MallOrderListVo getOrderDetail(Long orderId) {
        MallOrder mallOrder = mallOrderService.getOrderById(orderId);
        List<MallOrderItem> items = mallOrderItemService.getOrderItemByOrderId(mallOrder.getId());
        MallOrderItem firstItem = items == null || items.isEmpty() ? null : items.getFirst();
        return buildOrderListVo(mallOrder, firstItem);
    }

    private MallOrderListVo buildOrderListVo(MallOrder order, MallOrderItem item) {
        MallOrderListVo target = copyProperties(order, MallOrderListVo.class);
        if (target == null || item == null) {
            return target;
        }
        MallOrderListVo.ProductInfo productInfo = MallOrderListVo.ProductInfo.builder()
                .productName(item.getProductName())
                .productImage(item.getImageUrl())
                .quantity(item.getQuantity())
                .build();
        target.setProductInfo(productInfo);
        return target;
    }
}
