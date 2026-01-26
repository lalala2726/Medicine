package cn.zhangchuangla.medicine.client.elasticsearch.service.impl;

import cn.zhangchuangla.medicine.client.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.client.elasticsearch.repository.MallProductSearchRepository;
import cn.zhangchuangla.medicine.client.elasticsearch.service.MallProductSearchService;
import cn.zhangchuangla.medicine.client.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
        criteria = applyPriceCriteria(criteria, request);

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(pageNum - 1, pageSize));
        Sort sort = buildSort(request);
        if (sort.isSorted()) {
            query.addSort(sort);
        }

        return elasticsearchOperations.search(query, MallProductDocument.class);
    }

    @Override
    public List<String> suggest(String keyword, int size) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        int limit = Math.max(1, Math.min(size, MAX_SUGGEST_SIZE));
        String normalizedKeyword = keyword.trim();

        Criteria criteria = new Criteria("name").startsWith(normalizedKeyword)
                .or(new Criteria("categoryName").startsWith(normalizedKeyword))
                .or(new Criteria("name").matches(normalizedKeyword));

        CriteriaQuery query = new CriteriaQuery(criteria);
        int fetchSize = Math.min(Math.max(limit * 2, limit), MAX_PAGE_SIZE);
        query.setPageable(PageRequest.of(0, fetchSize));

        SearchHits<MallProductDocument> searchHits = elasticsearchOperations.search(query, MallProductDocument.class);
        if (searchHits.isEmpty()) {
            return List.of();
        }

        Set<String> suggestions = new LinkedHashSet<>();
        for (SearchHit<MallProductDocument> hit : searchHits) {
            MallProductDocument content = hit.getContent();
            addSuggestion(content.getName(), suggestions, limit);
            addSuggestion(content.getCategoryName(), suggestions, limit);
            if (suggestions.size() >= limit) {
                break;
            }
        }

        return suggestions.stream().limit(limit).toList();
    }

    private void addSuggestion(String value, Set<String> suggestions, int limit) {
        if (!StringUtils.hasText(value) || suggestions.size() >= limit) {
            return;
        }
        suggestions.add(value.trim());
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

    private Criteria applyPriceCriteria(Criteria criteria, MallProductSearchRequest request) {
        if (request.getPrice() != null) {
            return criteria.and(new Criteria("price").is(request.getPrice()));
        }
        BigDecimal minPrice = request.getMinPrice();
        BigDecimal maxPrice = request.getMaxPrice();
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "最低价格不能大于最高价格");
        }
        if (minPrice != null) {
            criteria = criteria.and(new Criteria("price").greaterThanEqual(minPrice));
        }
        if (maxPrice != null) {
            criteria = criteria.and(new Criteria("price").lessThanEqual(maxPrice));
        }
        return criteria;
    }

    private Sort buildSort(MallProductSearchRequest request) {
        Sort.Direction priceDirection = parseDirection(request.getPriceSort());
        Sort.Direction salesDirection = parseDirection(request.getSalesSort());
        if (priceDirection != null && salesDirection != null) {
            return Sort.by(new Sort.Order(priceDirection, "price"), new Sort.Order(salesDirection, "sales"));
        }
        if (priceDirection != null) {
            return Sort.by(new Sort.Order(priceDirection, "price"));
        }
        if (salesDirection != null) {
            return Sort.by(new Sort.Order(salesDirection, "sales"));
        }
        return Sort.unsorted();
    }

    private Sort.Direction parseDirection(String direction) {
        if (!StringUtils.hasText(direction)) {
            return null;
        }
        String normalized = direction.trim().toLowerCase();
        if ("asc".equals(normalized)) {
            return Sort.Direction.ASC;
        }
        if ("desc".equals(normalized)) {
            return Sort.Direction.DESC;
        }
        return null;
    }
}
