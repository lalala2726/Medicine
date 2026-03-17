package cn.zhangchuangla.medicine.agent.service.client.impl;

import cn.zhangchuangla.medicine.agent.service.client.ClientAgentProductService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductPurchaseCardsDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSearchDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSpecDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import cn.zhangchuangla.medicine.rpc.client.ClientAgentProductRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户端智能体商品服务 Dubbo Consumer 实现。
 */
@Service
public class ClientAgentProductServiceImpl implements ClientAgentProductService {

    /**
     * 商品模块 Dubbo RPC 引用。
     */
    @DubboReference(group = "medicine-client", version = "1.0.0", check = false, timeout = 3000, retries = 0,
            url = "${dubbo.references.medicine-client.url:}")
    private ClientAgentProductRpcService clientAgentProductRpcService;

    /**
     * 调用商品模块执行搜索，并将 RPC 分页转换为 MyBatis Plus 分页对象。
     *
     * @param request 搜索参数
     * @return 商品分页结果
     */
    @Override
    public Page<ClientAgentProductSearchDto> searchProducts(ClientAgentProductSearchRequest request) {
        ClientAgentProductSearchRequest safeRequest = request == null ? new ClientAgentProductSearchRequest() : request;
        PageResult<ClientAgentProductSearchDto> result = clientAgentProductRpcService.searchProducts(safeRequest);
        return toPage(result);
    }

    /**
     * 调用商品模块查询商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @Override
    public MallProductDetailDto getProductDetail(Long productId) {
        return clientAgentProductRpcService.getProductDetail(productId);
    }

    /**
     * 调用商品模块查询商品购买卡片补全结果。
     *
     * @param productIds 商品ID列表
     * @return 商品购买卡片补全结果
     */
    @Override
    public ClientAgentProductPurchaseCardsDto getProductPurchaseCards(List<Long> productIds) {
        ClientAgentProductPurchaseCardsDto result = clientAgentProductRpcService.getProductPurchaseCards(productIds);
        if (result == null) {
            return emptyProductPurchaseCards();
        }
        if (result.getItems() == null) {
            result.setItems(List.of());
        }
        if (result.getTotalPrice() == null) {
            result.setTotalPrice("0.00");
        }
        return result;
    }

    /**
     * 调用商品模块查询商品规格属性。
     *
     * @param productId 商品ID
     * @return 商品规格属性
     */
    @Override
    public ClientAgentProductSpecDto getProductSpec(Long productId) {
        return clientAgentProductRpcService.getProductSpec(productId);
    }

    /**
     * 将 RPC 分页结果转换为 MyBatis Plus Page，便于 controller 统一返回表格结构。
     *
     * @param result RPC 分页结果
     * @return MyBatis Plus 分页对象
     */
    private Page<ClientAgentProductSearchDto> toPage(PageResult<ClientAgentProductSearchDto> result) {
        if (result == null) {
            return new Page<>(1, 10, 0);
        }
        long pageNum = result.getPageNum() == null ? 1L : result.getPageNum();
        long pageSize = result.getPageSize() == null ? 10L : result.getPageSize();
        long total = result.getTotal() == null ? 0L : result.getTotal();
        Page<ClientAgentProductSearchDto> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(result.getRows() == null ? List.of() : result.getRows());
        return page;
    }

    /**
     * 构造空购买卡片结果。
     *
     * @return 空购买卡片结果
     */
    private ClientAgentProductPurchaseCardsDto emptyProductPurchaseCards() {
        return ClientAgentProductPurchaseCardsDto.builder()
                .totalPrice("0.00")
                .items(List.of())
                .build();
    }
}
