package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.service.MallCartService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 添加指定数量的商品到购物车
     *
     * @param productId 商品ID
     * @param quantity  添加数量（1-999）
     * @return 添加结果
     */
    @PostMapping("/{productId}/{quantity}")
    @Operation(summary = "添加指定数量商品到购物车", description = "支持批量添加商品")
    public AjaxResult<?> addProductWithQuantity(
            @Parameter(description = "商品ID", required = true)
            @PathVariable("productId") Long productId,
            @Parameter(description = "添加数量", required = true)
            @PathVariable("quantity") Integer quantity) {

        // 参数验证
        if (quantity == null || quantity <= 0) {
            return error("添加数量必须大于0");
        }
        if (quantity > 999) {
            return error("单次添加数量不能超过999");
        }

        boolean result = cartService.addProduct(productId, quantity);
        return toAjax(result);
    }
}
