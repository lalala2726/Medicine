package cn.zhangchuangla.medicine.common.elasticsearch.service;

import cn.zhangchuangla.medicine.common.elasticsearch.document.MallProductDocument;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

/**
 * 商品搜索服务，封装对 Elasticsearch 的读写操作。
 */
public interface MallProductSearchService {

    /**
     * 保存或更新商品文档。
     *
     * @param document 商品文档
     */
    void save(MallProductDocument document);

    /**
     * 批量保存或更新商品文档。
     *
     * @param documents 文档列表
     */
    void saveAll(List<MallProductDocument> documents);

    /**
     * 删除商品文档。
     *
     * @param productId 商品ID
     */
    void deleteById(Long productId);

    /**
     * 关键字搜索。
     *
     * @param keyword 关键字
     * @param page    页码，从0开始
     * @param size    页大小
     * @return 命中文档（包含总数）
     */
    SearchHits<MallProductDocument> search(String keyword, int page, int size);

}
