package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.agent.model.dto.ProductSalesDto;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单项 Mapper 接口。
 * <p>
 * 提供订单项数据的数据访问操作，用于统计商品销量等数据。
 *
 * @author Chuang
 */
@Mapper
public interface MallOrderItemMapper extends BaseMapper<MallOrderItem> {

    /**
     * 按商品 ID 聚合已完成订单的销量。
     *
     * @param productIds 商品 ID 列表
     * @return 商品销量统计列表
     */
    List<ProductSalesDto> getProductSalesByIds(@Param("productIds") List<Long> productIds);
}
