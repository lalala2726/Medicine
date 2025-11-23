package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallOrderItemMapper;
import cn.zhangchuangla.medicine.client.service.MallOrderItemService;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}



