package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Chuang
 */
public interface MallOrderMapper extends BaseMapper<MallOrder> {

    /**
     * 订单列表
     *
     * @param request 订单列表参数
     * @return 订单列表
     */
    Page<MallOrder> orderList(Page<MallOrder> mallOrderPage, @Param("request") MallOrderListRequest request);


    /**
     * 订单列表（带商品信息）
     *
     * @param request 订单列表参数
     * @return 订单列表
     */
    Page<OrderWithProductDto> orderListWithProduct(Page<OrderWithProductDto> orderWithProductDtoPage, @Param("request") MallOrderListRequest request);

    /**
     * 获取过期的订单
     *
     * @param expiredTime 过期时间, 单位毫秒,小于此时间则视为过期
     * @return 过期的订单
     */
    List<MallOrder> getExpiredOrderClean(long expiredTime);

    /**
     * 获取用户已付款的订单
     *
     * @param userId 用户id
     * @return 用户已付款的订单
     */
    Page<MallOrder> getPaidOrderPage(Page<MallOrder> page, @Param("userId") Long userId);
}




