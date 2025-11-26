package cn.zhangchuangla.medicine.admin.task;

import cn.zhangchuangla.medicine.admin.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.common.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.common.elasticsearch.service.MallProductSearchService;
import cn.zhangchuangla.medicine.common.elasticsearch.support.MallProductDocumentConverter;
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
    private final MallProductSearchService mallProductSearchService;

    /**
     * 异步写入/更新商品索引。
     */
    @Async
    public void reindexAsync(Long productId) {
        if (productId == null) {
            return;
        }
        MallProductDetailDto detail = mallProductMapper.getMallProductDetailById(productId);
        if (detail == null) {
            log.warn("Skip reindexing, product {} not found", productId);
            return;
        }
        MallProductDocument document = MallProductDocumentConverter.from(detail);
        mallProductSearchService.save(document);
        log.info("Indexed product {} into Elasticsearch", productId);
    }

    /**
     * 异步删除商品索引。
     */
    @Async
    public void removeAsync(Collection<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return;
        }
        productIds.forEach(mallProductSearchService::deleteById);
        log.info("Removed {} products from Elasticsearch", productIds.size());
    }
}
