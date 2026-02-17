package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.agent.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.vo.mall.MallProductListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin 端智能体商品工具控制器。
 * <p>
 * 提供给管理端智能体使用的商品查询工具接口，
 * 支持商品搜索、列表查询和详情查询等功能。
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/agent/product")
@Tag(name = "Admin智能体商品工具", description = "用于 Admin 侧智能体商品查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
public class AdminAgentProductToolsController extends BaseController {

    private final MallProductService agentProductService;

    /**
     * 商品搜索占位接口。
     * <p>
     * 此接口为智能体工具调用的占位接口，
     * 实际搜索逻辑由智能体通过 list 接口实现。
     *
     * @return 空结果
     */
    @GetMapping("/search")
    @Operation(summary = "商品搜索", description = "根据关键词和分类搜索商品")
    @PreAuthorize("hasAuthority('mall:product:list') or hasRole('super_admin')")
    public AjaxResult<Void> searchProduct() {
        return success();
    }

    /**
     * 根据条件分页查询商品列表。
     * <p>
     * 支持按关键词、分类等条件筛选商品，
     * 返回商品基本信息及封面图片，按分页形式返回。
     *
     * @param request 查询请求参数
     * @return 商品列表分页数据
     */
    @GetMapping("/list")
    @Operation(summary = "商品列表", description = "根据关键词和分类搜索商品")
    @PreAuthorize("hasAuthority('mall:product:list') or hasRole('super_admin')")
    public AjaxResult<TableDataResult> searchProducts(MallProductListQueryRequest request) {
        MallProductListQueryRequest safeRequest = request == null ? new MallProductListQueryRequest() : request;
        Page<MallProductDetailDto> page = agentProductService.listProducts(safeRequest);
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
     * 根据商品 ID 批量查询商品详情。
     * <p>
     * 返回商品的详细信息，包括基本信息、分类、图片列表等，
     * 不包含药品的详细说明书信息。
     *
     * @param productIds 商品 ID 列表，支持批量查询
     * @return 商品详情列表
     */
    @GetMapping("/{productIds}")
    @Operation(summary = "获取商品详情", description = "根据商品ID获取详细信息（不含药品详情）")
    @PreAuthorize("hasAuthority('mall:product:query') or hasRole('super_admin')")
    public AjaxResult<List<AdminAgentProductDetailVo>> getProductDetail(
            @Parameter(description = "商品ID")
            @PathVariable List<Long> productIds
    ) {
        return success(agentProductService.getProductDetail(productIds));
    }
}
