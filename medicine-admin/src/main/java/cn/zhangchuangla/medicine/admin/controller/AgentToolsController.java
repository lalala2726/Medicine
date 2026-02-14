package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.admin.model.vo.MallOrderListVo;
import cn.zhangchuangla.medicine.admin.model.vo.MallProductVo;
import cn.zhangchuangla.medicine.admin.service.AgentToolsService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import cn.zhangchuangla.medicine.model.vo.mall.MallProductListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI Agent 工具接口控制器
 * 为外部 Python AI Agent 提供统一的数据查询接口
 *
 * @author Chuang
 * <p>
 * created on 2026/2/1
 */
@RestController
@RequestMapping("/agent/tools")
@Tag(name = "智能体工具", description = "用于给智能体提供工具接口")
public class AgentToolsController extends BaseController {

    private final AgentToolsService agentToolsService;

    public AgentToolsController(AgentToolsService agentToolsService) {
        this.agentToolsService = agentToolsService;
    }


    @GetMapping("/current_user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public AjaxResult<UserVo> getCurrentUser() {
        User user = agentToolsService.getCurrentUser();
        UserVo userVo = copyProperties(user, UserVo.class);
        return success(userVo);
    }


    @GetMapping("/products/search")
    @Operation(summary = "搜索商品", description = "根据关键词和分类搜索商品")
    public AjaxResult<TableDataResult> searchProducts(MallProductListQueryRequest request) {
        Page<MallProductDetailDto> page = agentToolsService.searchProducts(request);
        List<MallProductListVo> mallProductListVos = page.getRecords().stream()
                .map(product -> {
                    MallProductListVo productListVo = copyProperties(product, MallProductListVo.class);
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        productListVo.setCoverImage(product.getImages().getFirst());
                    }
                    return productListVo;
                })
                .toList();
        return getTableData(page, mallProductListVos);
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "获取商品详情", description = "根据商品ID获取详细信息")
    public AjaxResult<MallProductVo> getProductDetail(
            @Parameter(description = "商品ID")
            @PathVariable Long productId
    ) {
        MallProductDetailDto product = agentToolsService.getProductDetail(productId);
        MallProductVo productVo = copyProperties(product, MallProductVo.class);
        return success(productVo);
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/orders/list")
    @Operation(summary = "获取订单列表", description = "分页获取订单列表，默认按创建时间倒序")
    public AjaxResult<TableDataResult> getOrderList(MallOrderListRequest request) {
        Page<OrderWithProductDto> orderPage = agentToolsService.getOrderList(request);
        List<MallOrderListVo> orderListVos = orderPage.getRecords().stream()
                .map(this::buildOrderListVo)
                .toList();
        return getTableData(orderPage, orderListVos);
    }


    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{orderId}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取详细信息")
    public AjaxResult<MallOrderListVo> getOrderDetail(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId
    ) {
        MallOrderListVo orderVo = agentToolsService.getOrderDetail(orderId);
        return success(orderVo);
    }

    private MallOrderListVo buildOrderListVo(OrderWithProductDto source) {
        MallOrderListVo target = copyProperties(source, MallOrderListVo.class);
        if (target == null) {
            return null;
        }
        if (source.getProductId() == null) {
            return target;
        }
        MallOrderListVo.ProductInfo productInfo = MallOrderListVo.ProductInfo.builder()
                .productName(source.getProductName())
                .productImage(source.getProductImage())
                .productPrice(source.getProductPrice())
                .productCategory(source.getProductCategory())
                .productId(source.getProductId())
                .quantity(source.getProductQuantity())
                .build();
        target.setProductInfo(productInfo);
        return target;
    }

}
