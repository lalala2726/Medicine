package cn.zhangchuangla.medicine.common.elasticsearch.service.impl;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.common.elasticsearch.repository.MallProductSearchRepository;
import cn.zhangchuangla.medicine.common.elasticsearch.service.MallProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品搜索服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallProductSearchServiceImpl implements MallProductSearchService {

    private static final int MAX_PAGE_SIZE = 50;

    private final MallProductSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void save(MallProductDocument document) {
        if (document == null || document.getId() == null) {
            log.warn("Skip saving empty product document: {}", document);
            return;
        }
        searchRepository.save(document);
    }

    @Override
    public void saveAll(List<MallProductDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        searchRepository.saveAll(documents);
    }

    @Override
    public void deleteById(Long productId) {
        if (productId == null) {
            return;
        }
        searchRepository.deleteById(productId);
    }

    @Override
    public SearchHits<MallProductDocument> search(String keyword, int page, int size) {
        if (!StringUtils.hasText(keyword)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR,"关键字不能不为空");
        }
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int pageIndex = Math.max(page, 0);

        Criteria keywordCriteria = buildKeywordCriteria(keyword);
        Criteria criteria = keywordCriteria.and(new Criteria("status").is(1));

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(pageIndex, pageSize));

        return elasticsearchOperations.search(query, MallProductDocument.class);
    }

    /**
     * 构建检索条件：名称、品牌、药品功效等字段均可命中。
     */
    private Criteria buildKeywordCriteria(String keyword) {
        String normalizedKeyword = keyword.trim();
        boolean hasWhitespace = normalizedKeyword.chars().anyMatch(Character::isWhitespace);

        // 使用 match 查询让中文分词命中，例如“感冒药”可以匹配拆分后的词条
        Criteria[] matchFields = {
                new Criteria("name").matches(normalizedKeyword),
                new Criteria("commonName").matches(normalizedKeyword),
                new Criteria("brand").matches(normalizedKeyword),
                new Criteria("efficacy").matches(normalizedKeyword),
                new Criteria("composition").matches(normalizedKeyword),
                new Criteria("usageMethod").matches(normalizedKeyword),
                new Criteria("warmTips").matches(normalizedKeyword),
                new Criteria("instruction").matches(normalizedKeyword)
        };
        // 同时兼容 keyword 子字段的前缀匹配，适配“快克”这类短品牌词
        Criteria[] keywordPrefixFields = {
                new Criteria("name.keyword").startsWith(normalizedKeyword),
                new Criteria("commonName.keyword").startsWith(normalizedKeyword),
                new Criteria("brand.keyword").startsWith(normalizedKeyword)
        };
        Criteria keywordCriteria = combineWithOr(matchFields);

        // startsWith 不允许带空格的词，存在空格时仅使用分词匹配以避免 InvalidDataAccessApiUsageException
        if (!hasWhitespace) {
            keywordCriteria = keywordCriteria.or(combineWithOr(keywordPrefixFields));
        }

        return keywordCriteria;
    }

    private Criteria combineWithOr(Criteria[] matchFields) {
        Criteria combined = matchFields[0];
        for (int i = 1; i < matchFields.length; i++) {
            combined = combined.or(matchFields[i]);
        }
        return combined;
    }
}
