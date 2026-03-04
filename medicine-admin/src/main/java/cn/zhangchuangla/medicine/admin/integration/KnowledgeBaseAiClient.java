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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 知识库 AI 服务调用客户端。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseAiClient {

    private static final String CREATE_PATH = "/knowledge_base";

    private final KnowledgeBaseAiProperties properties;

    /**
     * 调用 AI 服务创建知识库。
     */
    public void createKnowledgeBase(String milvusCollectionName, Integer embeddingDim, String description) {
        Assert.notEmpty(milvusCollectionName, "Milvus集合名称不能为空");
        Assert.isPositive(embeddingDim, "向量维度必须大于0");

        String url = buildUrl();
        CreateKnowledgeBasePayload payload = new CreateKnowledgeBasePayload(milvusCollectionName, embeddingDim, description);
        String requestBody = JSONUtils.toJson(payload);
        try {
            HttpResult<String> result = executeCreateRequest(url, requestBody);
            BaseResponse.extractData(result);
        } catch (HttpClientException ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "调用AI服务创建知识库失败: " + ex.getMessage());
        }
    }

    /**
     * 发送创建知识库请求，并在可用时透传当前登录态的 Authorization 头。
     *
     * @param url  AI 服务完整请求地址
     * @param body JSON 请求体
     * @return HTTP 响应结果
     */
    HttpResult<String> executeCreateRequest(String url, String body) {
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

    /**
     * 构建 AI 创建知识库接口完整 URL。
     *
     * @return 形如 <a href="http://host:port/knowledge_base">...</a> 的完整地址
     */
    private String buildUrl() {
        String baseUrl = properties.getBaseUrl();
        Assert.notEmpty(baseUrl, "knowledge-base.ai.base-url 未配置");
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + KnowledgeBaseAiClient.CREATE_PATH;
        }
        return baseUrl + KnowledgeBaseAiClient.CREATE_PATH;
    }

    private record CreateKnowledgeBasePayload(String knowledge_name, Integer embedding_dim, String description) {
    }
}
