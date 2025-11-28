package cn.zhangchuangla.medicine.common.elasticsearch.service.impl;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.common.elasticsearch.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.common.elasticsearch.repository.MallProductSearchRepository;
import cn.zhangchuangla.medicine.common.elasticsearch.service.MallProductSearchService;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 商品搜索服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallProductSearchServiceImpl implements MallProductSearchService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_SUGGEST_SIZE = 20;

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
    public SearchHits<MallProductDocument> search(MallProductSearchRequest request) {
        // 限制最大查询深度，防止深分页导致性能问题
        int maxOffset = 500;
        int pageNum = Math.max(request.getPageNum(), 1);
        int pageSize = Math.min(Math.max(request.getPageSize(), 1), MAX_PAGE_SIZE);
        long offset = (long) (pageNum - 1) * pageSize;
        if (offset + pageSize > maxOffset) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "查询数据总数不能超过" + maxOffset + "条");
        }

        if (!StringUtils.hasText(request.getKeyword())) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "关键字不能为空");
        }

        Criteria criteria = buildKeywordCriteria(request.getKeyword());
        // 默认仅查询上架商品，传了 status 则使用入参
        if (request.getStatus() != null) {
            criteria = criteria.and(new Criteria("status").is(request.getStatus()));
        } else {
            criteria = criteria.and(new Criteria("status").is(1));
        }

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(pageNum - 1, pageSize));

        return elasticsearchOperations.search(query, MallProductDocument.class);
    }

    @Override
    public List<String> suggest(String keyword, int size) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        int limit = Math.max(1, Math.min(size, MAX_SUGGEST_SIZE));
        String normalizedKeyword = keyword.trim();

        Suggester suggester = Suggester.of(builder -> builder
                .text(normalizedKeyword)
                .suggesters("name-suggest",
                        fs -> fs.completion(c -> c
                                .field("nameSuggest")
                                .skipDuplicates(true)
                                .size(limit)
                        )
                )
                .suggesters("common-name-suggest",
                        fs -> fs.completion(c -> c
                                .field("commonNameSuggest")
                                .skipDuplicates(true)
                                .size(limit)
                        )
                )
                .suggesters("brand-suggest",
                        fs -> fs.completion(c -> c
                                .field("brandSuggest")
                                .skipDuplicates(true)
                                .size(limit)
                        )
                )
        );


        NativeQuery query = NativeQuery.builder()
                .withSuggester(suggester)
                .withMaxResults(0)
                .build();

        SearchHits<MallProductDocument> searchHits = elasticsearchOperations.search(query, MallProductDocument.class);
        Suggest suggest = searchHits.getSuggest();
        if (suggest == null) {
            return List.of();
        }

        Set<String> suggestions = new LinkedHashSet<>();
        collectSuggestionTexts(suggest, "name-suggest", suggestions, limit);
        collectSuggestionTexts(suggest, "common-name-suggest", suggestions, limit);
        collectSuggestionTexts(suggest, "brand-suggest", suggestions, limit);

        return suggestions.stream().limit(limit).toList();
    }

    private void collectSuggestionTexts(Suggest suggest, String key, Set<String> accumulator, int limit) {
        Suggest.Suggestion<?> rawSuggestion = suggest.getSuggestion(key);
        if (!(rawSuggestion instanceof CompletionSuggestion<?> completionSuggestion)) {
            return;
        }

        for (CompletionSuggestion.Entry<?> entry : completionSuggestion.getEntries()) {
            for (CompletionSuggestion.Entry.Option<?> option : entry.getOptions()) {
                accumulator.add(option.getText());
                if (accumulator.size() >= limit) {
                    return;
                }
            }
        }
    }


    /**
     * 构建检索条件：名称（含拼音）、分类名、品牌、通用名、功效均可命中。
     */
    private Criteria buildKeywordCriteria(String keyword) {
        String normalizedKeyword = keyword.trim();
        boolean hasWhitespace = normalizedKeyword.chars().anyMatch(Character::isWhitespace);

        Criteria[] matchFields = {
                new Criteria("name").matches(normalizedKeyword),
                new Criteria("categoryName").matches(normalizedKeyword),
                new Criteria("brand").matches(normalizedKeyword),
                new Criteria("commonName").matches(normalizedKeyword),
                new Criteria("efficacy").matches(normalizedKeyword)
        };
        Criteria keywordCriteria = combineWithOr(matchFields);

        if (!hasWhitespace) {
            Criteria[] keywordPrefixFields = {
                    new Criteria("name.keyword").startsWith(normalizedKeyword),
                    new Criteria("categoryName").startsWith(normalizedKeyword),
                    new Criteria("brand.keyword").startsWith(normalizedKeyword),
                    new Criteria("commonName.keyword").startsWith(normalizedKeyword)
            };
            keywordCriteria = keywordCriteria.or(combineWithOr(keywordPrefixFields));
        }

        return keywordCriteria;
    }

    private Criteria combineWithOr(Criteria[] criteriaArr) {
        Criteria combined = Objects.requireNonNull(criteriaArr[0]);
        for (int i = 1; i < criteriaArr.length; i++) {
            combined = combined.or(criteriaArr[i]);
        }
        return combined;
    }
}
