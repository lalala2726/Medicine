package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallOrderShippingMapper;
import cn.zhangchuangla.medicine.admin.service.MallOrderShippingService;
import cn.zhangchuangla.medicine.model.entity.MallOrderShipping;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 订单物流服务实现
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Slf4j
@Service
public class MallOrderShippingServiceImpl extends ServiceImpl<MallOrderShippingMapper, MallOrderShipping>
        implements MallOrderShippingService {

    @Override
    public MallOrderShipping getByOrderId(Long orderId) {
        return lambdaQuery()
                .eq(MallOrderShipping::getOrderId, orderId)
                .one();
    }

    @Override
    public boolean createShipping(MallOrderShipping shipping) {
        return save(shipping);
    }
}

