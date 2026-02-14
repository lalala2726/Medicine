package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.admin.model.vo.AgentDrugDetailVo;
import cn.zhangchuangla.medicine.admin.model.vo.AgentProductDetailVo;
import cn.zhangchuangla.medicine.admin.model.vo.MallOrderListVo;
import cn.zhangchuangla.medicine.admin.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.admin.service.MallProductService;
import cn.zhangchuangla.medicine.admin.service.UserService;
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
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Tag(name = "智能体工具", description = "用于给智能体提供工具接口")
public class AgentToolsController extends BaseController {

    private final UserService userService;
    private final MallProductService mallProductService;
    private final MallOrderService mallOrderService;


    /**
     * 获取当前登录用户的详情
     *
     * @return 用户详情
     */
    @GetMapping("/current_user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public AjaxResult<UserVo> getCurrentUser() {
        Long userId = getUserId();
        User user = userService.getUserById(userId);
        UserVo userVo = copyProperties(user, UserVo.class);
        return success(userVo);
    }

    /**
     * 根据关键字和查询条件查询商品信息，相比商品列表接口，此接口调用ES在搜索的话会更加高效
     *
     * @return 商品列表
     */
    @GetMapping("/product/search")
    @Operation(summary = "商品搜索", description = "根据关键词和分类搜索商品")
    public AjaxResult<Void> searchProduct() {
        // todo 商品搜索功能待实现
        return success();
    }


    /**
     * 根据条件查询商品列表
     *
     * @param request 请求参数
     * @return 商品列表
     */
    @GetMapping("/product/list")
    @Operation(summary = "商品列表", description = "根据关键词和分类搜索商品")
    public AjaxResult<TableDataResult> searchProducts(MallProductListQueryRequest request) {
        Page<MallProductDetailDto> page = mallProductService.listMallProductWithCategory(request);
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

    /**
     * 根据商品ID查询商品详情
     *
     * @return 返回商品详情
     */
    @GetMapping("/product/{productIds}")
    @Operation(summary = "获取商品详情", description = "根据商品ID获取详细信息（不含药品详情）")
    public AjaxResult<List<AgentProductDetailVo>> getProductDetail(
            @Parameter(description = "商品ID")
            @PathVariable List<Long> productIds
    ) {
        List<MallProductDetailDto> products = mallProductService.getMallProductByIds(productIds);
        List<AgentProductDetailVo> productVos = copyListProperties(products, AgentProductDetailVo.class);
        return success(productVos);
    }

    /**
     * 根据商品ID查询药品详情
     *
     * @return 返回药品详情
     */
    @GetMapping("/drug/{productIds}")
    @Operation(summary = "获取药品详情", description = "根据商品ID获取药品详细信息")
    public AjaxResult<List<AgentDrugDetailVo>> getDrugDetail(
            @Parameter(description = "商品ID")
            @PathVariable List<Long> productIds
    ) {
        List<AgentDrugDetailVo> drugDetails = mallProductService.getDrugDetailByProductIds(productIds);
        return success(drugDetails);
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/order/list")
    @Operation(summary = "获取订单列表", description = "分页获取订单列表，默认按创建时间倒序")
    public AjaxResult<TableDataResult> getOrderList(MallOrderListRequest request) {
        MallOrderListRequest safeRequest = request == null ? new MallOrderListRequest() : request;
        Page<OrderWithProductDto> orderPage = mallOrderService.orderWithProduct(safeRequest);
        List<MallOrderListVo> orderListVos = orderPage.getRecords().stream()
                .map(this::buildOrderListVo)
                .toList();
        return getTableData(orderPage, orderListVos);
    }


    /**
     * 获取订单详情
     */
    @GetMapping("/order/{orderIds}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取详细信息")
    public AjaxResult<List<OrderDetailVo>> getOrderDetail(@Parameter(description = "订单ID") @PathVariable List<Long> orderIds) {
        List<OrderDetailVo> orderDetails = mallOrderService.getOrderDetailByIds(orderIds);
        return success(orderDetails);
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
