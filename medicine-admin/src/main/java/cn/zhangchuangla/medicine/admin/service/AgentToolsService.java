package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.admin.model.vo.MallOrderListVo;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * AI Agent 工具服务接口
 * 为外部 AI Agent 提供统一的数据查询接口
 *
 * @author Chuang
 * <p>
 * created on 2026/2/1
 */
public interface AgentToolsService {

    /**
     * 获取当前用户信息
     *
     * @return 返回当前用户信息
     */
    User getCurrentUser();

    /**
     * 根据条件搜索商品
     *
     * @param request 查询参数
     * @return 商品列表
     */
    Page<MallProductDetailDto> searchProducts(MallProductListQueryRequest request);

    /**
     * 获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    MallProductDetailDto getProductDetail(Long productId);

    /**
     * 获取订单列表
     *
     * @param request 查询参数
     * @return 订单分页列表
     */
    Page<OrderWithProductDto> getOrderList(MallOrderListRequest request);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    MallOrderListVo getOrderDetail(Long orderId);
}
