package cn.zhangchuangla.medicine.client.mapper;

import cn.zhangchuangla.medicine.client.model.dto.ProductSalesDto;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Chuang
 */
public interface MallOrderItemMapper extends BaseMapper<MallOrderItem> {

    /**
     * 查询已完成订单的商品销量
     */
    List<ProductSalesDto> listCompletedSalesByProductIds(@Param("productIds") List<Long> productIds);

}
