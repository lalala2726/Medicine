package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.request.UpdateCartQuantityRequest;
import cn.zhangchuangla.medicine.client.model.vo.CartItemVo;
import cn.zhangchuangla.medicine.client.service.MallCartService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/15
 */
@RestController
@RequestMapping("/mall/cart")
@Tag(name = "购物车接口", description = "购物车接口")
@RequiredArgsConstructor
public class MallCartController extends BaseController {

    private final MallCartService cartService;

    /**
     * 添加商品到购物车（默认数量为1）
     *
     * @param productId 商品ID
     * @return 添加结果
     */
    @PostMapping("/{productId}")
    @Operation(summary = "添加商品到购物车", description = "默认添加1个商品")
    public AjaxResult<?> addProduct(
            @Parameter(description = "商品ID", required = true)
            @PathVariable("productId") Long productId) {
        boolean result = cartService.addProduct(productId);
        return toAjax(result);
    }


    /**
     * 更新购物车商品数量
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新购物车商品数量")
    public AjaxResult<Void> updateCartQuantity(@Validated @RequestBody UpdateCartQuantityRequest request) {
        boolean result = cartService.updateCartQuantity(request);
        return toAjax(result);
    }

    /**
     * 获取购物车列表
     *
     * @return 购物车商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取购物车列表")
    public AjaxResult<List<CartItemVo>> getCartList() {
        List<CartItemVo> cartList = cartService.getCartList();
        return success(cartList);
    }

    /**
     * 删除购物车商品
     *
     * @param cartIds 购物车ID列表
     * @return 删除结果
     */
    @DeleteMapping("/remove")
    @Operation(summary = "删除购物车商品")
    public AjaxResult<?> removeCartItems(@RequestBody List<Long> cartIds) {
        boolean result = cartService.removeCartItems(cartIds);
        return toAjax(result);
    }
}
