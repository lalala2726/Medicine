package cn.zhangchuangla.medicine.client.spi;

import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.llm.model.tool.client.ClientMallProductOut;
import cn.zhangchuangla.medicine.llm.model.tool.client.ClientSearchMallProductOut;
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


    @Override
    public List<ClientSearchMallProductOut> searchMallProducts(String keyword, int limit) {
        if (keyword.isBlank()) {
            return List.of();
        }
        // 最大查询50个数据
        limit = Math.max(1, Math.min(limit, 50));
        return mallProductService.SearchDetail(keyword, limit);
    }

    @Override
    public ClientMallProductOut getMallProductById(Long id) {
        MallProductDetailDto product = mallProductService.getProductAndDrugInfoById(id);
        if (product == null) {
            return null;
        }
        List<String> images = product.getImages();
        return ClientMallProductOut.builder()
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
    public List<ClientMallProductOut> getMallProductById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .distinct()
                .map(this::getMallProductById)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
