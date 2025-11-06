package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallOrderItemMapper;
import cn.zhangchuangla.medicine.admin.service.MallOrderItemService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Chuang
 */
@Service
public class MallOrderItemServiceImpl extends ServiceImpl<MallOrderItemMapper, MallOrderItem>
        implements MallOrderItemService {
    @Override
    public List<MallOrderItem> getOrderItemByOrderId(Long orderId) {
        Assert.isPositive(orderId, "订单ID不能小于0");
        List<MallOrderItem> list = list(new LambdaQueryWrapper<MallOrderItem>()
                .in(MallOrderItem::getOrderId, orderId));
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ResponseResultCode.DATA_NOT_FOUND, "订单项不存在");
        }
        return list;

    }
}




