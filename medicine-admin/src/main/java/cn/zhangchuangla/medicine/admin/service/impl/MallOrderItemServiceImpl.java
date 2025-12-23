package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallOrderItemMapper;
import cn.zhangchuangla.medicine.admin.model.dto.ProductSalesDto;
import cn.zhangchuangla.medicine.admin.service.MallOrderItemService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class MallOrderItemServiceImpl extends ServiceImpl<MallOrderItemMapper, MallOrderItem>
        implements MallOrderItemService {

    private final MallOrderItemMapper mallOrderItemMapper;

    @Override
    public List<MallOrderItem> getOrderItemByOrderId(Long orderId) {
        Assert.isPositive(orderId, "订单ID不能小于0");
        List<MallOrderItem> list = list(new LambdaQueryWrapper<MallOrderItem>()
                .in(MallOrderItem::getOrderId, orderId));
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ResponseCode.DATA_NOT_FOUND, "订单项不存在");
        }
        return list;

    }

    @Override
    public List<ProductSalesDto> getProductSales() {
        return mallOrderItemMapper.getProductSales();
    }

    @Override
    public Integer getCompletedSalesByProductId(Long productId) {
        if (productId == null) {
            return 0;
        }
        return getCompletedSalesByProductIds(List.of(productId)).getOrDefault(productId, 0);
    }

    @Override
    public Map<Long, Integer> getCompletedSalesByProductIds(List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return Collections.emptyMap();
        }
        List<ProductSalesDto> sales = mallOrderItemMapper.getProductSalesByIds(productIds);
        if (CollectionUtils.isEmpty(sales)) {
            return Collections.emptyMap();
        }
        return sales.stream()
                .filter(item -> item.getProductId() != null)
                .collect(Collectors.toMap(
                        ProductSalesDto::getProductId,
                        item -> item.getSales() != null ? item.getSales() : 0,
                        (left, right) -> right
                ));
    }
}



