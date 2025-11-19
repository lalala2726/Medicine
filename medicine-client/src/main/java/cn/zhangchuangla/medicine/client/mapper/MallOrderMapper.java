package cn.zhangchuangla.medicine.client.mapper;

import cn.zhangchuangla.medicine.client.model.dto.MallOrderDto;
import cn.zhangchuangla.medicine.client.model.request.OrderListRequest;
import cn.zhangchuangla.medicine.client.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface MallOrderMapper extends BaseMapper<MallOrder> {

    /**
     * 分页查询用户订单列表
     *
     * @param page    分页对象
     * @param request 查询条件
     * @param userId  用户ID
     * @return 订单列表DTO
     */
    Page<MallOrderDto> selectOrderList(@Param("page") Page<MallOrderDto> page,
                                       @Param("request") OrderListRequest request,
                                       @Param("userId") Long userId);

    /**
     * 查询订单详情
     *
     * @param orderNo 订单编号
     * @param userId  用户ID
     * @return 订单详情
     */
    OrderDetailVo selectOrderDetail(@Param("orderNo") String orderNo,
                                    @Param("userId") Long userId);
}




