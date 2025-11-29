package cn.zhangchuangla.medicine.client.spi;

import cn.zhangchuangla.medicine.client.model.request.OrderListRequest;
import cn.zhangchuangla.medicine.client.model.vo.MallProductSearchVo;
import cn.zhangchuangla.medicine.client.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.client.model.vo.OrderListVo;
import cn.zhangchuangla.medicine.client.model.vo.OrderListVo.OrderItemSimpleVo;
import cn.zhangchuangla.medicine.client.service.MallOrderService;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.elasticsearch.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.llm.model.response.card.MedicineCardItem;
import cn.zhangchuangla.medicine.llm.model.response.card.ProductCardItem;
import cn.zhangchuangla.medicine.llm.spi.ClientDataProvider;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductWithImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * client 模块实现的 SPI，向 LLM 提供真实商品/订单数据，用于生成卡片消息。
 */
@Component
@RequiredArgsConstructor
public class ClientDataProviderImpl implements ClientDataProvider {

    private static final int MAX_LIMIT = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private final MallProductService mallProductService;
    private final MallOrderService mallOrderService;

    @Override
    public List<MedicineCardItem> recommendMedicines(String keyword, int limit) {
        List<MallProductWithImageDto> products = searchProductsInternal(keyword, limit);
        return products.stream()
                .map(this::toMedicineCardItem)
                .toList();
    }

    @Override
    public List<ProductCardItem> searchProducts(String keyword, int limit) {
        List<MallProductWithImageDto> products = searchProductsInternal(keyword, limit);
        return products.stream()
                .map(this::toProductCardItem)
                .toList();
    }

    /**
     * 获取当前用户最近的订单
     *
     * @param limit 查询数量
     * @return 最近的订单
     */
    @Override
    public List<ProductCardItem> latestOrders(int limit) {
        OrderListRequest request = new OrderListRequest();
        request.setPageNum(1);
        request.setPageSize(normalizeLimit(limit));

        List<OrderListVo> orders;
        try {
            orders = mallOrderService.getOrderList(request).getRecords();
        } catch (Exception ex) {
            return List.of();
        }
        if (CollectionUtils.isEmpty(orders)) {
            return List.of();
        }
        return orders.stream()
                .sorted(Comparator.comparing(OrderListVo::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toOrderCardItem)
                .toList();
    }

    @Override
    public Optional<ProductCardItem> findProductById(String productId) {
        if (!StringUtils.hasText(productId)) {
            return Optional.empty();
        }

        // 1) 尝试按商品ID查询
        try {
            Long id = Long.parseLong(productId);
            MallProductWithImageDto product = mallProductService.getProductWithImagesById(id);
            if (product != null) {
                return Optional.of(toProductCardItem(product));
            }
        } catch (NumberFormatException ignored) {
            // not a product id, fall through to order lookup
        }

        // 2) 按订单号查询
        try {
            OrderDetailVo orderDetail = mallOrderService.getOrderDetail(productId);
            if (orderDetail != null) {
                return Optional.of(toOrderCardItem(orderDetail));
            }
        } catch (Exception ignored) {
            // 保持 silent，统一返回 empty
        }
        return Optional.empty();
    }

    private List<MallProductWithImageDto> searchProductsInternal(String keyword, int limit) {
        int safeLimit = normalizeLimit(limit);

        MallProductSearchRequest request = new MallProductSearchRequest();
        request.setPageNum(1);
        request.setPageSize(safeLimit);
        request.setKeyword(keyword);

        PageResult<MallProductSearchVo> page = mallProductService.search(request);
        List<MallProductSearchVo> records = page == null ? List.of() : page.getRows();
        if (CollectionUtils.isEmpty(records)) {
            return List.of();
        }

        List<MallProductWithImageDto> result = new ArrayList<>();
        for (MallProductSearchVo vo : records) {
            if (vo.getProductId() == null) {
                continue;
            }
            MallProductWithImageDto detail = mallProductService.getProductWithImagesById(vo.getProductId());
            if (detail != null) {
                result.add(detail);
            }
        }
        return result;
    }

    private MedicineCardItem toMedicineCardItem(MallProductWithImageDto product) {
        DrugDetailDto drug = product.getDrugDetail();

        List<String> tags = new ArrayList<>();
        if (drug != null && StringUtils.hasText(drug.getBrand())) {
            tags.add(drug.getBrand());
        }
        if (product.getStock() != null) {
            tags.add("库存" + product.getStock());
        }

        return MedicineCardItem.builder()
                .id(String.valueOf(product.getId()))
                .name(product.getName())
                .image(firstImage(product))
                .price(product.getPrice())
                .originalPrice(null)
                .spec(drug != null && StringUtils.hasText(drug.getPackaging()) ? drug.getPackaging() : product.getUnit())
                .efficacy(drug != null ? drug.getEfficacy() : null)
                .prescription(drug != null ? drug.getPrescription() : null)
                .tags(tags)
                .stock(product.getStock())
                .build();
    }

    private ProductCardItem toProductCardItem(MallProductWithImageDto product) {
        List<String> tags = new ArrayList<>();
        if (product.getStock() != null) {
            tags.add("库存" + product.getStock());
        }
        DrugDetailDto drug = product.getDrugDetail();
        if (drug != null && StringUtils.hasText(drug.getBrand())) {
            tags.add(drug.getBrand());
        }
        return ProductCardItem.builder()
                .id(String.valueOf(product.getId()))
                .name(product.getName())
                .image(firstImage(product))
                .price(product.getPrice())
                .quantity(null)
                .tags(tags)
                .build();
    }

    private ProductCardItem toOrderCardItem(OrderListVo order) {
        OrderItemSimpleVo firstItem = CollectionUtils.isEmpty(order.getItems()) ? null : order.getItems().getFirst();
        String name = firstItem == null
                ? "订单号：" + order.getOrderNo()
                : "订单号：" + order.getOrderNo() + " - " + firstItem.getProductName();
        List<String> tags = new ArrayList<>();
        if (StringUtils.hasText(order.getOrderStatusName())) {
            tags.add(order.getOrderStatusName());
        }
        if (order.getCreateTime() != null) {
            tags.add(DATE_FORMATTER.format(order.getCreateTime().toInstant()));
        }
        return ProductCardItem.builder()
                .id(order.getOrderNo())
                .name(name)
                .image(firstItem != null ? firstItem.getImageUrl() : null)
                .price(Optional.ofNullable(order.getPayAmount()).orElse(order.getTotalAmount()))
                .quantity(firstItem != null ? firstItem.getQuantity() : null)
                .tags(tags)
                .build();
    }

    private ProductCardItem toOrderCardItem(OrderDetailVo order) {
        OrderDetailVo.OrderItemDetailVo firstItem = CollectionUtils.isEmpty(order.getItems()) ? null : order.getItems().getFirst();
        String name = firstItem == null
                ? "订单号：" + order.getOrderNo()
                : "订单号：" + order.getOrderNo() + " - " + firstItem.getProductName();

        int quantitySum = 0;
        if (!CollectionUtils.isEmpty(order.getItems())) {
            quantitySum = order.getItems().stream()
                    .map(OrderDetailVo.OrderItemDetailVo::getQuantity)
                    .filter(Objects::nonNull)
                    .reduce(0, Integer::sum);
        }

        List<String> tags = new ArrayList<>();
        if (StringUtils.hasText(order.getOrderStatusName())) {
            tags.add(order.getOrderStatusName());
        }
        if (order.getCreateTime() != null) {
            tags.add(DATE_FORMATTER.format(order.getCreateTime().toInstant()));
        }

        return ProductCardItem.builder()
                .id(order.getOrderNo())
                .name(name)
                .image(firstItem != null ? firstItem.getImageUrl() : null)
                .price(Optional.ofNullable(order.getPayAmount()).orElse(order.getTotalAmount()))
                .quantity(quantitySum == 0 ? null : quantitySum)
                .tags(tags)
                .build();
    }

    private String firstImage(MallProductWithImageDto product) {
        if (product.getProductImages() == null || product.getProductImages().isEmpty()) {
            return null;
        }
        return product.getProductImages().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(cn.zhangchuangla.medicine.model.entity.MallProductImage::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(cn.zhangchuangla.medicine.model.entity.MallProductImage::getId, Comparator.nullsLast(Long::compareTo)))
                .map(cn.zhangchuangla.medicine.model.entity.MallProductImage::getImageUrl)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 5;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
