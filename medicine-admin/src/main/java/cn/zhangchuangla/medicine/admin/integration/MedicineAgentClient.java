package cn.zhangchuangla.medicine.admin.integration;

import cn.zhangchuangla.medicine.admin.config.KnowledgeBaseAiProperties;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.core.utils.JSONUtils;
import cn.zhangchuangla.medicine.common.http.RequestClient;
import cn.zhangchuangla.medicine.common.http.exception.HttpClientException;
import cn.zhangchuangla.medicine.common.http.model.BaseResponse;
import cn.zhangchuangla.medicine.common.http.model.ClientRequest;
import cn.zhangchuangla.medicine.common.http.model.HttpMethod;
import cn.zhangchuangla.medicine.common.http.model.HttpResult;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库 Agent 服务调用客户端。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineAgentClient {

    private static final String CREATE_PATH = "/knowledge_base";
    private static final String LOAD_PATH = "/knowledge_base/load";
    private static final String RELEASE_PATH = "/knowledge_base/release";
    private static final String DOCUMENT_CHUNK_LIST_PATH = "/knowledge_base/document/chunks/list";
    private static final int MIN_EMBEDDING_DIM = 128;
    private static final int MAX_EMBEDDING_DIM = 1 << 13;
    private static final int CHUNK_PAGE_SIZE = 50;
    private static final int MAX_CHUNK_PAGE = 10_000;
    private static final Type DOCUMENT_CHUNK_PAGE_DATA_TYPE = new TypeToken<DocumentChunkPageData>() {
    }.getType();

    private final KnowledgeBaseAiProperties properties;

    /**
     * 调用 Agent 服务创建知识库。
     */
    public void createKnowledgeBase(String knowledgeName, Integer embeddingDim, String description) {
        Assert.notEmpty(knowledgeName, "知识库名称不能为空");
        validateEmbeddingDim(embeddingDim);

        String url = buildUrl(CREATE_PATH);
        CreateKnowledgeBasePayload payload = new CreateKnowledgeBasePayload(knowledgeName, embeddingDim, description);
        String requestBody = JSONUtils.toJson(payload);
        doPostWithValidation(url, requestBody, "调用Agent服务创建知识库失败: ");
    }

    /**
     * 调用 Agent 服务启用（Load）知识库集合。
     */
    public void loadKnowledgeBase(String knowledgeName) {
        Assert.notEmpty(knowledgeName, "知识库名称不能为空");
        String url = buildUrl(LOAD_PATH);
        String requestBody = JSONUtils.toJson(new CollectionPayload(knowledgeName));
        doPostWithValidation(url, requestBody, "调用Agent服务启用知识库失败: ");
    }

    /**
     * 调用 Agent 服务关闭（Release）知识库集合。
     */
    public void releaseKnowledgeBase(String knowledgeName) {
        Assert.notEmpty(knowledgeName, "知识库名称不能为空");
        String url = buildUrl(RELEASE_PATH);
        String requestBody = JSONUtils.toJson(new CollectionPayload(knowledgeName));
        doPostWithValidation(url, requestBody, "调用Agent服务关闭知识库失败: ");
    }

    /**
     * 分页拉取文档切片列表。
     *
     * @param knowledgeName 知识库名称
     * @param documentId    文档ID
     * @return 全量切片行
     */
    public List<DocumentChunkRow> listDocumentChunks(String knowledgeName, Long documentId) {
        Assert.notEmpty(knowledgeName, "知识库名称不能为空");
        Assert.isPositive(documentId, "文档ID必须大于0");

        String url = buildUrl(DOCUMENT_CHUNK_LIST_PATH);
        List<DocumentChunkRow> rows = new ArrayList<>();
        int page = 1;
        while (true) {
            DocumentChunkPageData pageData = fetchDocumentChunkPage(url, knowledgeName, documentId, page, CHUNK_PAGE_SIZE);
            if (pageData != null && pageData.getRows() != null && !pageData.getRows().isEmpty()) {
                rows.addAll(pageData.getRows());
            }
            if (pageData == null || !Boolean.TRUE.equals(pageData.getHas_next())) {
                return rows;
            }
            page++;
            if (page > MAX_CHUNK_PAGE) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "文档切片分页超过上限");
            }
        }
    }

    /**
     * 发送 POST 请求，并验证响应结果。
     *
     * @param url         Agent 服务完整请求地址
     * @param body        JSON 请求体
     * @param errorPrefix 错误信息前缀
     */
    private void doPostWithValidation(String url, String body, String errorPrefix) {
        try {
            HttpResult<String> result = executePostRequest(url, body);
            BaseResponse.extractData(result);
        } catch (HttpClientException ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, errorPrefix + ex.getMessage());
        }
    }

    private DocumentChunkPageData fetchDocumentChunkPage(String url, String knowledgeName,
                                                         Long documentId, int page, int pageSize) {
        try {
            ClientRequest request = buildChunkListRequest(url, knowledgeName, documentId, page, pageSize);
            HttpResult<String> result = executeGetRequest(request);
            return BaseResponse.extractData(result, DOCUMENT_CHUNK_PAGE_DATA_TYPE);
        } catch (HttpClientException ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "调用Agent服务拉取文档切片失败: " + ex.getMessage());
        }
    }

    /**
     * 发送 Agent POST 请求，并在可用时透传当前登录态的 Authorization 头。
     *
     * @param url  Agent 服务完整请求地址
     * @param body JSON 请求体
     * @return HTTP 响应结果
     */
    HttpResult<String> executePostRequest(String url, String body) {
        ClientRequest.Builder requestBuilder = ClientRequest.builder()
                .method(HttpMethod.POST)
                .url(url)
                .body(body);

        Authentication authentication = SecurityUtils.getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                String token = SecurityUtils.getToken();
                if (token != null && !token.isBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                }
            } catch (RuntimeException ignored) {
                // 非 Web 请求上下文下可能无 request，按无鉴权头继续
                log.error("非 Web 请求上下文下可能无 request，按无鉴权头无法请求 {}", url);
            }
        }
        return RequestClient.post(requestBuilder.build(), String.class);
    }

    ClientRequest buildChunkListRequest(String url, String knowledgeName, Long documentId, int page, int pageSize) {
        return ClientRequest.builder()
                .method(HttpMethod.GET)
                .url(url)
                .addQueryParameter("knowledge_name", knowledgeName)
                .addQueryParameter("document_id", String.valueOf(documentId))
                .addQueryParameter("page", String.valueOf(page))
                .addQueryParameter("page_size", String.valueOf(pageSize))
                .build();
    }

    HttpResult<String> executeGetRequest(ClientRequest request) {
        return RequestClient.get(request, String.class);
    }

    /**
     * 构建 Agent 接口完整 URL。
     *
     * @param path 接口路径
     * @return 形如 <a href="http://host:port/knowledge_base">...</a> 的完整地址
     */
    private String buildUrl(String path) {
        String baseUrl = properties.getBaseUrl();
        Assert.notEmpty(baseUrl, "knowledge-base.ai.base-url 未配置");
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        return baseUrl + path;
    }

    /**
     * 校验向量维度：范围 [128, 8192] 且必须为 2 的幂。
     *
     * @param embeddingDim 向量维度
     */
    private void validateEmbeddingDim(Integer embeddingDim) {
        Assert.notNull(embeddingDim, "向量维度不能为空");
        Assert.isParamTrue(embeddingDim >= MIN_EMBEDDING_DIM && embeddingDim <= MAX_EMBEDDING_DIM,
                "向量维度必须在128到8192之间");
        Assert.isParamTrue((embeddingDim & (embeddingDim - 1)) == 0, "向量维度必须是2的幂");
    }

    private record CreateKnowledgeBasePayload(String knowledge_name, Integer embedding_dim, String description) {
    }

    private record CollectionPayload(String collection_name) {
    }

    @Data
    public static class DocumentChunkPageData {
        private List<DocumentChunkRow> rows;
        private Integer total;
        private Integer page_num;
        private Integer page_size;
        private Boolean has_next;
    }

    @Data
    public static class DocumentChunkRow {
        private Long id;
        private Long document_id;
        private Integer chunk_index;
        private String content;
        private Integer char_count;
        private String chunk_strategy;
        private Integer chunk_size;
        private Integer token_size;
        private String source_hash;
        private Long created_at_ts;
    }
}
