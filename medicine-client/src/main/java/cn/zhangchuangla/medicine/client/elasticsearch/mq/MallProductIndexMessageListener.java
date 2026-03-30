package cn.zhangchuangla.medicine.client.elasticsearch.mq;

import cn.zhangchuangla.medicine.client.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.client.elasticsearch.service.MallProductSearchService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.ProductIndexQueueConstants;
import cn.zhangchuangla.medicine.model.mq.ProductIndexMessage;
import cn.zhangchuangla.medicine.model.mq.ProductIndexOperation;
import cn.zhangchuangla.medicine.model.mq.ProductIndexPayload;
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
 *
 * @author Chuang
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MallProductIndexMessageListener {

    /**
     * 商品搜索服务。
     */
    private final MallProductSearchService mallProductSearchService;

    /**
     * 监听商品索引消息。
     *
     * @param message 商品索引消息
     */
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

    /**
     * 处理商品索引新增或更新。
     *
     * @param payload 商品索引载荷
     */
    private void handleUpsert(ProductIndexPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("跳过商品索引更新操作，payload 为空");
            return;
        }
        mallProductSearchService.save(toDocument(payload));
    }

    /**
     * 处理商品索引删除。
     *
     * @param productIds 商品ID集合
     */
    private void handleDelete(Collection<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return;
        }
        productIds.forEach(mallProductSearchService::deleteById);
    }

    /**
     * 将索引载荷转换为 ES 文档。
     *
     * @param payload 商品索引载荷
     * @return ES 文档
     */
    private MallProductDocument toDocument(ProductIndexPayload payload) {
        return MallProductDocument.builder()
                .id(payload.getId())
                .name(payload.getName())
                .categoryName(payload.getCategoryName())
                .categoryId(payload.getCategoryId())
                .price(payload.getPrice())
                .prescription(payload.getPrescription())
                .status(payload.getStatus())
                .brand(payload.getBrand())
                .commonName(payload.getCommonName())
                .nameSuggest(completion(payload.getName()))
                .brandSuggest(completion(payload.getBrand()))
                .commonNameSuggest(completion(payload.getCommonName()))
                .efficacy(payload.getEfficacy())
                .tagIds(payload.getTagIds())
                .tagNames(payload.getTagNames())
                .tagTypeBindings(payload.getTagTypeBindings())
                .sales(payload.getSales())
                .instruction(payload.getInstruction())
                .coverImage(payload.getCoverImage())
                .build();
    }

    /**
     * 构建自动补全字段。
     *
     * @param value 原始值
     * @return 自动补全对象
     */
    private Completion completion(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return new Completion(List.of(value));
    }
}
