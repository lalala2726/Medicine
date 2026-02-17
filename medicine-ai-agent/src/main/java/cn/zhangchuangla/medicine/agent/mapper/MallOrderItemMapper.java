package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.agent.model.dto.ProductSalesDto;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MallOrderItemMapper extends BaseMapper<MallOrderItem> {

    /**
     * 按商品 ID 聚合已完成订单销量。
     */
    List<ProductSalesDto> getProductSalesByIds(@Param("productIds") List<Long> productIds);
}
