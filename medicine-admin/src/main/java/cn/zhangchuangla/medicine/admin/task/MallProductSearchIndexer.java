package cn.zhangchuangla.medicine.admin.task;

import cn.zhangchuangla.medicine.admin.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.admin.publisher.ProductIndexMessagePublisher;
import cn.zhangchuangla.medicine.admin.service.MallOrderItemService;
import cn.zhangchuangla.medicine.admin.service.MallProductTagService;
import cn.zhangchuangla.medicine.model.constants.MallProductTagConstants;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.mq.ProductIndexPayload;
import cn.zhangchuangla.medicine.model.vo.MallProductTagVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 将后台商品数据异步同步至 Elasticsearch 的任务。
 *
 * @author Chuang
 * @see cn.zhangchuangla.medicine.client.elasticsearch.mq.MallProductIndexMessageListener
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MallProductSearchIndexer {

    /**
     * 商品 Mapper。
     */
    private final MallProductMapper mallProductMapper;

    /**
     * 订单项服务。
     */
    private final MallOrderItemService mallOrderItemService;

    /**
     * 商品标签服务。
     */
    private final MallProductTagService mallProductTagService;

    /**
     * 商品索引消息发布器。
     */
    private final ProductIndexMessagePublisher productIndexMessagePublisher;

    /**
     * 异步写入或更新商品索引。
     *
     * @param productId 商品ID
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
        detail.setTags(mallProductTagService.listEnabledTagVoMapByProductIds(List.of(productId)).getOrDefault(productId, List.of()));
        productIndexMessagePublisher.publishUpsert(toPayload(detail));
        log.info("向 RabbitMQ 发布商品 {} 的索引事件", productId);
    }

    /**
     * 异步删除商品索引。
     *
     * @param productIds 商品ID集合
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
     * 批量发布商品索引事件。
     *
     * @param products 商品详情列表
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
        Map<Long, List<MallProductTagVo>> tagMap = mallProductTagService.listEnabledTagVoMapByProductIds(productIds);
        products.forEach(product -> {
            if (product == null || product.getId() == null) {
                return;
            }
            product.setTags(tagMap.getOrDefault(product.getId(), List.of()));
        });
        products.stream()
                .map(this::toPayload)
                .filter(payload -> payload != null && payload.getId() != null)
                .forEach(productIndexMessagePublisher::publishUpsert);
        log.info("向 RabbitMQ 发布 {} 个商品的索引事件", products.size());
    }

    /**
     * 将商品详情转换为索引载荷。
     *
     * @param detail 商品详情
     * @return 索引载荷
     */
    private ProductIndexPayload toPayload(MallProductDetailDto detail) {
        if (detail == null) {
            return null;
        }
        return ProductIndexPayload.builder()
                .id(detail.getId())
                .name(detail.getName())
                .categoryId(detail.getCategoryId())
                .price(detail.getPrice())
                .sales(detail.getSales())
                .status(detail.getStatus())
                .categoryName(detail.getCategoryName())
                .prescription(detail.getDrugDetail() != null ? detail.getDrugDetail().getPrescription() : null)
                .brand(detail.getDrugDetail() != null ? detail.getDrugDetail().getBrand() : null)
                .commonName(detail.getDrugDetail() != null ? detail.getDrugDetail().getCommonName() : null)
                .efficacy(detail.getDrugDetail() != null ? detail.getDrugDetail().getEfficacy() : null)
                .tagIds(extractTagIds(detail.getTags()))
                .tagNames(extractTagNames(detail.getTags()))
                .tagTypeBindings(extractTagTypeBindings(detail.getTags()))
                .instruction(detail.getDrugDetail() != null ? detail.getDrugDetail().getInstruction() : null)
                .coverImage(detail.getImages() != null && !detail.getImages().isEmpty() ? detail.getImages().getFirst() : null)
                .build();
    }

    /**
     * 提取标签ID列表。
     *
     * @param tags 标签列表
     * @return 标签ID列表
     */
    private List<Long> extractTagIds(List<MallProductTagVo> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return List.of();
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(MallProductTagVo::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 提取标签名称列表。
     *
     * @param tags 标签列表
     * @return 标签名称列表
     */
    private List<String> extractTagNames(List<MallProductTagVo> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return List.of();
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(MallProductTagVo::getName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 提取标签类型绑定列表。
     *
     * @param tags 标签列表
     * @return 标签类型绑定列表
     */
    private List<String> extractTagTypeBindings(List<MallProductTagVo> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return List.of();
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .filter(tag -> tag.getId() != null && StringUtils.hasText(tag.getTypeCode()))
                .map(tag -> tag.getTypeCode() + MallProductTagConstants.TYPE_BINDING_SEPARATOR + tag.getId())
                .distinct()
                .toList();
    }
}
