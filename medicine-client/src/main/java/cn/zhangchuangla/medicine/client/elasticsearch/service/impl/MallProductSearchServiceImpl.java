package cn.zhangchuangla.medicine.client.elasticsearch.service.impl;

import cn.zhangchuangla.medicine.client.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.client.elasticsearch.repository.MallProductSearchRepository;
import cn.zhangchuangla.medicine.client.elasticsearch.service.MallProductSearchService;
import cn.zhangchuangla.medicine.client.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.constants.MallProductTagConstants;
import cn.zhangchuangla.medicine.model.dto.MallProductTagFilterGroup;
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
import java.util.*;

/**
 * 商品搜索服务实现。
 *
 * @author Chuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallProductSearchServiceImpl implements MallProductSearchService {

    /**
     * 单次查询允许的最大页大小。
     */
    private static final int MAX_PAGE_SIZE = 50;

    /**
     * 自动补全允许的最大返回数量。
     */
    private static final int MAX_SUGGEST_SIZE = 20;

    /**
     * 商品搜索仓库。
     */
    private final MallProductSearchRepository searchRepository;

    /**
     * Elasticsearch 操作对象。
     */
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 保存商品索引文档。
     *
     * @param document 商品索引文档
     */
    @Override
    public void save(MallProductDocument document) {
        if (document == null || document.getId() == null) {
            log.warn("Skip saving empty product document: {}", document);
            return;
        }
        searchRepository.save(document);
    }

    /**
     * 批量保存商品索引文档。
     *
     * @param documents 商品索引文档列表
     */
    @Override
    public void saveAll(List<MallProductDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        searchRepository.saveAll(documents);
    }

    /**
     * 删除商品索引文档。
     *
     * @param productId 商品ID
     */
    @Override
    public void deleteById(Long productId) {
        if (productId == null) {
            return;
        }
        searchRepository.deleteById(productId);
    }

    /**
     * 搜索商品索引。
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    @Override
    public SearchHits<MallProductDocument> search(MallProductSearchRequest request) {
        int maxOffset = 500;
        int pageNum = Math.max(request.getPageNum(), 1);
        int pageSize = Math.min(Math.max(request.getPageSize(), 1), MAX_PAGE_SIZE);
        long offset = (long) (pageNum - 1) * pageSize;
        if (offset + pageSize > maxOffset) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "查询数据总数不能超过" + maxOffset + "条");
        }
        if (!hasSearchCondition(request)) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "搜索条件不能为空");
        }

        Criteria criteria = buildSearchCriteria(request);
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

    /**
     * 商品搜索自动补全。
     *
     * @param keyword 搜索关键字
     * @param size    返回数量
     * @return 自动补全结果
     */
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

    /**
     * 追加自动补全候选项。
     *
     * @param value       原始值
     * @param suggestions 候选集合
     * @param limit       返回上限
     */
    private void addSuggestion(String value, Set<String> suggestions, int limit) {
        if (!StringUtils.hasText(value) || suggestions.size() >= limit) {
            return;
        }
        suggestions.add(value.trim());
    }

    /**
     * 构建检索条件。
     *
     * @param request 搜索请求
     * @return 检索条件
     */
    private Criteria buildSearchCriteria(MallProductSearchRequest request) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(request.getKeyword())) {
            criteriaList.add(buildKeywordCriteria(request.getKeyword()));
        }
        if (StringUtils.hasText(request.getCategoryName())) {
            criteriaList.add(new Criteria("categoryName").is(request.getCategoryName().trim()));
        }
        if (request.getCategoryId() != null) {
            criteriaList.add(new Criteria("categoryId").is(request.getCategoryId()));
        }
        if (StringUtils.hasText(request.getEfficacy())) {
            criteriaList.add(buildUsageCriteria(request.getEfficacy()));
        }
        appendTagCriteria(criteriaList, request.getTagFilterGroups());

        Criteria criteria = criteriaList.getFirst();
        for (int i = 1; i < criteriaList.size(); i++) {
            criteria = criteria.and(criteriaList.get(i));
        }
        return criteria;
    }

    /**
     * 判断是否提供了至少一个可用搜索条件。
     *
     * @param request 搜索请求
     * @return 是否提供了有效搜索条件
     */
    private boolean hasSearchCondition(MallProductSearchRequest request) {
        return StringUtils.hasText(request.getKeyword())
                || StringUtils.hasText(request.getCategoryName())
                || request.getCategoryId() != null
                || StringUtils.hasText(request.getEfficacy())
                || !CollectionUtils.isEmpty(request.getTagIds());
    }

    /**
     * 构建商品用途或适用场景检索条件。
     *
     * @param usage 用途关键词
     * @return 检索条件
     */
    private Criteria buildUsageCriteria(String usage) {
        String normalizedUsage = usage.trim();
        return new Criteria("efficacy").matches(normalizedUsage)
                .or(new Criteria("instruction").matches(normalizedUsage));
    }

    /**
     * 构建关键字检索条件。
     *
     * @param keyword 搜索关键字
     * @return 关键字检索条件
     */
    private Criteria buildKeywordCriteria(String keyword) {
        String normalizedKeyword = keyword.trim();
        boolean hasWhitespace = normalizedKeyword.chars().anyMatch(Character::isWhitespace);

        Criteria[] matchFields = {
                new Criteria("name").matches(normalizedKeyword),
                new Criteria("categoryName").matches(normalizedKeyword),
                new Criteria("brand").matches(normalizedKeyword),
                new Criteria("commonName").matches(normalizedKeyword),
                new Criteria("efficacy").matches(normalizedKeyword),
                new Criteria("tagNames").matches(normalizedKeyword)
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

    /**
     * 以 OR 组合多个 Criteria。
     *
     * @param criteriaArr Criteria 数组
     * @return 组合后的 Criteria
     */
    private Criteria combineWithOr(Criteria[] criteriaArr) {
        Criteria combined = Objects.requireNonNull(criteriaArr[0]);
        for (int i = 1; i < criteriaArr.length; i++) {
            combined = combined.or(criteriaArr[i]);
        }
        return combined;
    }

    /**
     * 追加按类型分组后的标签过滤条件。
     *
     * @param criteriaList 条件列表
     * @param filterGroups 标签分组
     */
    private void appendTagCriteria(List<Criteria> criteriaList, List<MallProductTagFilterGroup> filterGroups) {
        if (CollectionUtils.isEmpty(filterGroups)) {
            return;
        }
        for (MallProductTagFilterGroup group : filterGroups) {
            if (group == null || !StringUtils.hasText(group.getTypeCode()) || CollectionUtils.isEmpty(group.getTagIds())) {
                continue;
            }
            Criteria[] criteriaArr = group.getTagIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(tagId -> new Criteria("tagTypeBindings").is(
                            group.getTypeCode() + MallProductTagConstants.TYPE_BINDING_SEPARATOR + tagId
                    ))
                    .toArray(Criteria[]::new);
            if (criteriaArr.length > 0) {
                criteriaList.add(combineWithOr(criteriaArr));
            }
        }
    }

    /**
     * 追加价格过滤条件。
     *
     * @param criteria 现有条件
     * @param request  搜索请求
     * @return 追加价格过滤后的条件
     */
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

    /**
     * 构建排序条件。
     *
     * @param request 搜索请求
     * @return 排序条件
     */
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

    /**
     * 解析排序方向。
     *
     * @param direction 排序方向
     * @return Spring Sort 排序方向
     */
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
