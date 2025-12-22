package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallOrderItemMapper;
import cn.zhangchuangla.medicine.client.model.dto.ProductSalesDto;
import cn.zhangchuangla.medicine.client.service.MallOrderItemService;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Chuang
 */
@Service
public class MallOrderItemServiceImpl extends ServiceImpl<MallOrderItemMapper, MallOrderItem>
        implements MallOrderItemService {


    @Override
    public List<MallOrderItem> getOrderItemByOrderId(Long orderId) {
        return Optional.ofNullable(orderId)
                .map(id -> lambdaQuery()
                        .eq(MallOrderItem::getOrderId, id)
                        .list())
                .orElse(List.of());
    }

    @Override
    public java.util.Map<Long, Integer> getCompletedSalesByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductSalesDto> stats = baseMapper.listCompletedSalesByProductIds(productIds);
        if (stats == null || stats.isEmpty()) {
            return Collections.emptyMap();
        }
        return stats.stream().collect(Collectors.toMap(ProductSalesDto::getProductId, ProductSalesDto::getSales));
    }

    @Override
    public Integer getCompletedSalesByProductId(Long productId) {
        if (productId == null) {
            return 0;
        }
        return getCompletedSalesByProductIds(List.of(productId)).getOrDefault(productId, 0);
    }
}

