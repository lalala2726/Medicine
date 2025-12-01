package cn.zhangchuangla.medicine.client.spi;

import cn.zhangchuangla.medicine.client.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.client.service.MallOrderService;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.llm.model.tool.client.MallProductTool;
import cn.zhangchuangla.medicine.llm.model.tool.client.OrderDetailTool;
import cn.zhangchuangla.medicine.llm.model.tool.client.SearchMallProductTool;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProvider;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * client 模块实现的 SPI，向 LLM 提供真实商品/订单数据，用于生成卡片消息。
 */
@Component
@RequiredArgsConstructor
public class ClientDataProviderImpl implements ClientDataProvider {

    private final MallProductService mallProductService;
    private final MallOrderService mallOrderService;


    @Override
    public List<SearchMallProductTool> searchMallProducts(String keyword, int limit) {
        if (keyword.isBlank()) {
            return List.of();
        }
        // 最大查询50个数据
        limit = Math.max(1, Math.min(limit, 50));
        return mallProductService.SearchDetail(keyword, limit);
    }

    @Override
    public MallProductTool getMallProductById(Long id) {
        MallProductDetailDto product = mallProductService.getProductAndDrugInfoById(id);
        if (product == null) {
            return null;
        }
        List<String> images = product.getImages();
        return MallProductTool.builder()
                .id(product.getId())
                .name(product.getName())
                .unit(product.getUnit())
                .price(product.getPrice())
                .status(product.getStatus())
                .deliveryType(product.getDeliveryType())
                .categoryName(product.getCategoryName())
                .drugDetail(product.getDrugDetail())
                .coverImage(images == null || images.isEmpty() ? null : images.getFirst())
                .build();
    }

    @Override
    public List<MallProductTool> getMallProductById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .distinct()
                .map(this::getMallProductById)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public OrderDetailTool getOrderDetailByOrderNo(String orderNo) {
        OrderDetailVo orderDetail = mallOrderService.getOrderDetail(orderNo);
        return BeanCotyUtils.copyProperties(orderDetail, OrderDetailTool.class);
    }
}
