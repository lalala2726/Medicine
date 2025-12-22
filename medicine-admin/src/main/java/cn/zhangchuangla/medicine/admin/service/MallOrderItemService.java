package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.dto.ProductSalesDto;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface MallOrderItemService extends IService<MallOrderItem> {


    /**
     * 根据订单ID列表查询订单项
     *
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<MallOrderItem> getOrderItemByOrderId(Long orderId);

    /**
     * 查询商品销售信息
     *
     * @return 商品销售信息列表
     */
    List<ProductSalesDto> getProductSales();

    /**
     * 获取指定商品的已完成订单销量。
     *
     * @param productId 商品ID
     * @return 已完成订单销量
     */
    Integer getCompletedSalesByProductId(Long productId);

    /**
     * 获取指定商品的已完成订单销量汇总。
     *
     * @param productIds 商品ID列表
     * @return 商品销量映射
     */
    java.util.Map<Long, Integer> getCompletedSalesByProductIds(List<Long> productIds);
}
