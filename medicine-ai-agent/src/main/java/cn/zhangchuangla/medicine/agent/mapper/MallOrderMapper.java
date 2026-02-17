package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MallOrderMapper extends BaseMapper<MallOrder> {

    /**
     * 订单列表（首个订单项商品信息）。
     */
    Page<OrderWithProductDto> orderListWithProduct(Page<OrderWithProductDto> page,
                                                   @Param("request") AdminMallOrderListRequest request);
}
