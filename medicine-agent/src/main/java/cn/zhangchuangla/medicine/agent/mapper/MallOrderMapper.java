package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单 Mapper 接口。
 * <p>
 * 提供订单数据的数据访问操作，包括基础 CRUD 和自定义查询。
 *
 * @author Chuang
 */
@Mapper
public interface MallOrderMapper extends BaseMapper<MallOrder> {

    /**
     * 分页查询订单列表（含首个订单项商品信息）。
     *
     * @param page    分页参数
     * @param request 查询请求参数
     * @return 订单与商品信息分页数据
     */
    Page<OrderWithProductDto> orderListWithProduct(Page<OrderWithProductDto> page,
                                                   @Param("request") AdminMallOrderListRequest request);
}
