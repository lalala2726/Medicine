package cn.zhangchuangla.medicine.ai.gateway.controller.admin;

import cn.zhangchuangla.medicine.ai.gateway.service.AdminOrderQueryService;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.request.graphql.GraphQLOrderQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * AI 网关订单 GraphQL 控制器
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
@Controller
@Validated
@IsAdmin
public class AdminOrderGraphQlController extends BaseController {

    private final AdminOrderQueryService adminOrderQueryService;

    public AdminOrderGraphQlController(AdminOrderQueryService adminOrderQueryService) {
        this.adminOrderQueryService = adminOrderQueryService;
    }

    /**
     * 订单分页查询。
     */
    @QueryMapping(name = "adminOrders")
    public TableDataResult adminOrders(@Argument @Valid GraphQLOrderQuery query) {
        Page<MallOrder> page = adminOrderQueryService.searchOrders(query);
        return getTableData(page, page.getRecords()).getData();
    }

    /**
     * 订单详情查询。
     */
    @QueryMapping(name = "adminOrder")
    public MallOrder adminOrder(@Argument @NotNull(message = "订单ID不能为空") @Positive(message = "订单ID必须大于0") Long id) {
        return adminOrderQueryService.getOrderById(id);
    }
}
