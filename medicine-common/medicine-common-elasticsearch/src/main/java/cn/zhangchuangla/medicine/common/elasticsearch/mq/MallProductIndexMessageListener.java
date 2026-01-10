package cn.zhangchuangla.medicine.common.elasticsearch.mq;

import cn.zhangchuangla.medicine.common.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.common.elasticsearch.service.MallProductSearchService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.ProductIndexQueueConstants;
import cn.zhangchuangla.medicine.model.mq.message.ProductIndexMessage;
import cn.zhangchuangla.medicine.model.mq.message.ProductIndexOperation;
import cn.zhangchuangla.medicine.model.mq.message.ProductIndexPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * 订阅商品索引事件，落库至 Elasticsearch。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MallProductIndexMessageListener {

    private final MallProductSearchService mallProductSearchService;

    @RabbitListener(queues = ProductIndexQueueConstants.QUEUE)
    public void onMessage(ProductIndexMessage message) {
        if (message == null || message.getOperation() == null) {
            log.warn("跳过商品索引消息: {}", message);
            return;
        }
        if (message.getOperation() == ProductIndexOperation.UPSERT) {
            handleUpsert(message.getPayload());
        } else if (message.getOperation() == ProductIndexOperation.DELETE) {
            handleDelete(message.getProductIds());
        }
    }

    private void handleUpsert(ProductIndexPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("跳过商品索引更新操作，payload 为空");
            return;
        }
        MallProductDocument document = toDocument(payload);
        mallProductSearchService.save(document);
    }

    private void handleDelete(Collection<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return;
        }
        productIds.forEach(mallProductSearchService::deleteById);
    }

    private MallProductDocument toDocument(ProductIndexPayload payload) {
        return MallProductDocument.builder()
                .id(payload.getId())
                .name(payload.getName())
                .categoryName(payload.getCategoryName())
                .price(payload.getPrice())
                .prescription(payload.getPrescription())
                .status(payload.getStatus())
                .brand(payload.getBrand())
                .commonName(payload.getCommonName())
                .nameSuggest(completion(payload.getName()))
                .brandSuggest(completion(payload.getBrand()))
                .commonNameSuggest(completion(payload.getCommonName()))
                .efficacy(payload.getEfficacy())
                .sales(payload.getSales())
                .instruction(payload.getInstruction())
                .coverImage(payload.getCoverImage())
                .build();
    }

    private Completion completion(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return new Completion(List.of(value));
    }
}
