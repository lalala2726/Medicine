package cn.zhangchuangla.medicine.agent.controller.client;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.vo.client.ClientAgentProductDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.client.ClientAgentProductSearchVo;
import cn.zhangchuangla.medicine.agent.model.vo.client.ClientAgentProductSpecVo;
import cn.zhangchuangla.medicine.agent.service.client.ClientAgentProductService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSearchDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSpecDto;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 客户端智能体商品工具控制器。
 */
@RestController
@RequestMapping("/agent/client/product")
@Tag(name = "客户端智能体商品工具", description = "用于客户端智能体商品查询接口")
@InternalAgentHeaderTrace
@Validated
@RequiredArgsConstructor
public class AgentClientProductController extends BaseController {


    /**
     * 客户端智能体商品服务。
     */
    private final ClientAgentProductService clientAgentProductService;

    /**
     * 按关键词搜索商品，供 AI 先定位商品后再进行详情查询。
     *
     * @param request 搜索参数
     * @return 商品搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "按关键词分页搜索商品")
    public AjaxResult<TableDataResult> searchProducts(@Validated ClientAgentProductSearchRequest request) {
        ClientAgentProductSearchRequest safeRequest = ClientAgentProductSearchRequest.sanitize(request);
        Page<ClientAgentProductSearchDto> page = clientAgentProductService.searchProducts(safeRequest);
        List<ClientAgentProductSearchVo> rows = copyListProperties(page, ClientAgentProductSearchVo.class);
        return getTableData(page, rows);
    }

    /**
     * 根据商品ID查询商品详情与药品说明信息。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/{productId}")
    @Operation(summary = "获取商品详情", description = "根据商品ID获取客户端智能体商品详情")
    public AjaxResult<ClientAgentProductDetailVo> getProductDetail(
            @PathVariable @Min(value = 1, message = "商品ID不能小于1") Long productId
    ) {
        MallProductDetailDto detail = clientAgentProductService.getProductDetail(productId);
        ClientAgentProductDetailVo target = copyProperties(detail, ClientAgentProductDetailVo.class);
        DrugDetailDto drugDetailDto = detail.getDrugDetail();
        if (drugDetailDto != null) {
            target.setDrugDetail(copyProperties(drugDetailDto, ClientAgentProductDetailVo.DrugDetail.class));
        }
        return success(target);
    }

    /**
     * 根据商品ID查询商品规格属性。
     *
     * @param productId 商品ID
     * @return 商品规格属性
     */
    @GetMapping("/spec/{productId}")
    @Operation(summary = "获取商品规格属性", description = "根据商品ID获取客户端智能体商品规格属性")
    public AjaxResult<ClientAgentProductSpecVo> getProductSpec(
            @PathVariable @Min(value = 1, message = "商品ID不能小于1") Long productId
    ) {
        ClientAgentProductSpecDto spec = clientAgentProductService.getProductSpec(productId);
        return success(copyProperties(spec, ClientAgentProductSpecVo.class));
    }
}
