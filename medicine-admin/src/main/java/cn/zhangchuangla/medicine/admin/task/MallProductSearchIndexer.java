package cn.zhangchuangla.medicine.admin.task;

import cn.zhangchuangla.medicine.admin.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.admin.publisher.ProductIndexMessagePublisher;
import cn.zhangchuangla.medicine.admin.service.MallOrderItemService;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.mq.message.ProductIndexPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 将后台商品数据异步同步至 Elasticsearch 的任务。
 *
 * @see cn.zhangchuangla.medicine.client.elasticsearch.mq.MallProductIndexMessageListener
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MallProductSearchIndexer {

    private final MallProductMapper mallProductMapper;
    private final MallOrderItemService mallOrderItemService;
    private final ProductIndexMessagePublisher productIndexMessagePublisher;

    /**
     * 异步写入/更新商品索引：admin 侧只负责发 MQ 消息。
     */
    @Async
    public void reindexAsync(Long productId) {
        if (productId == null) {
            return;
        }
        MallProductDetailDto detail = mallProductMapper.getMallProductDetailById(productId);
        if (detail == null) {
            log.warn("跳过重建索引，商品 {} 未找到", productId);
            return;
        }
        Map<Long, Integer> salesMap = mallOrderItemService.getCompletedSalesByProductIds(List.of(productId));
        detail.setSales(salesMap.getOrDefault(productId, 0));
        ProductIndexPayload payload = toPayload(detail);
        productIndexMessagePublisher.publishUpsert(payload);

        log.info("向 RabbitMQ 发布商品 {} 的索引事件", productId);
    }

    /**
     * 异步删除商品索引。
     */
    @Async
    public void removeAsync(Collection<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return;
        }
        productIndexMessagePublisher.publishDelete(productIds);

        log.info("向 RabbitMQ 发布 {} 个商品的删除事件", productIds.size());
    }

    /**
     * 批量发布商品索引事件
     */
    public void reindexBatch(Collection<MallProductDetailDto> products) {
        if (CollectionUtils.isEmpty(products)) {
            return;
        }
        List<Long> productIds = products.stream()
                .map(MallProductDetailDto::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, Integer> salesMap = mallOrderItemService.getCompletedSalesByProductIds(productIds);
        products.forEach(product -> {
            if (product == null || product.getId() == null) {
                return;
            }
            product.setSales(salesMap.getOrDefault(product.getId(), 0));
        });
        products.stream()
                .map(this::toPayload)
                .filter(payload -> payload != null && payload.getId() != null)
                .forEach(productIndexMessagePublisher::publishUpsert);

        log.info("向 RabbitMQ 发布 {} 个商品的索引事件", products.size());
    }

    private ProductIndexPayload toPayload(MallProductDetailDto mallProductDetailDto) {
        if (mallProductDetailDto == null) {
            return null;
        }
        return ProductIndexPayload.builder()
                .id(mallProductDetailDto.getId())
                .name(mallProductDetailDto.getName())
                .price(mallProductDetailDto.getPrice())
                .sales(mallProductDetailDto.getSales())
                .status(mallProductDetailDto.getStatus())
                // 可能为null的字段
                .categoryName(mallProductDetailDto.getCategoryName())
                .prescription(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getPrescription() : null)
                .brand(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getBrand() : null)
                .commonName(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getCommonName() : null)
                .efficacy(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getEfficacy() : null)
                .instruction(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getInstruction() : null)
                .coverImage(mallProductDetailDto.getImages() != null && !mallProductDetailDto.getImages().isEmpty() ? mallProductDetailDto.getImages().getFirst() : null)
                .build();
    }

}
