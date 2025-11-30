package cn.zhangchuangla.medicine.admin.task;

import cn.zhangchuangla.medicine.admin.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.common.rabbitmq.message.ProductIndexPayload;
import cn.zhangchuangla.medicine.common.rabbitmq.publisher.ProductIndexMessagePublisher;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * 将后台商品数据异步同步至 Elasticsearch 的任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MallProductSearchIndexer {

    private final MallProductMapper mallProductMapper;
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

    private ProductIndexPayload toPayload(MallProductDetailDto mallProductDetailDto) {
        if (mallProductDetailDto == null) {
            return null;
        }
        return ProductIndexPayload.builder()
                .id(mallProductDetailDto.getId())
                .name(mallProductDetailDto.getName())
                .price(mallProductDetailDto.getPrice())
                .status(mallProductDetailDto.getStatus())
                .prescription(mallProductDetailDto.getDrugDetail().getPrescription())
                // 可能为null的字段
                .categoryName(mallProductDetailDto.getCategoryName())
                .brand(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getBrand() : null)
                .commonName(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getCommonName() : null)
                .efficacy(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getEfficacy() : null)
                .instruction(mallProductDetailDto.getDrugDetail() != null ? mallProductDetailDto.getDrugDetail().getInstruction() : null)
                .coverImage(mallProductDetailDto.getImages() != null && !mallProductDetailDto.getImages().isEmpty() ? mallProductDetailDto.getImages().getFirst() : null)
                .build();
    }
}
