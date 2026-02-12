package cn.zhangchuangla.medicine.ai.gateway.controller.admin;

import cn.zhangchuangla.medicine.ai.gateway.service.AdminProductQueryService;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.graphql.GraphQLProductQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * AI 网关商品 GraphQL 控制器
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
@Controller
@Validated
@PreAuthorize("hasRole('admin') or hasRole('super_admin')")
public class AdminProductGraphQlController extends BaseController {

    private final AdminProductQueryService adminProductQueryService;

    public AdminProductGraphQlController(AdminProductQueryService adminProductQueryService) {
        this.adminProductQueryService = adminProductQueryService;
    }

    /**
     * 商品分页查询。
     */
    @QueryMapping(name = "adminProducts")
    public TableDataResult adminProducts(@Argument @Validated GraphQLProductQuery query) {
        Page<MallProduct> page = adminProductQueryService.searchProducts(query);
        return getTableData(page, page.getRecords()).getData();
    }

    /**
     * 商品详情查询。
     */
    @QueryMapping(name = "adminProduct")
    public MallProduct adminProduct(@Argument @NotNull(message = "商品ID不能为空") @Positive(message = "商品ID必须大于0") Long id) {
        return adminProductQueryService.getProductById(id);
    }
}
