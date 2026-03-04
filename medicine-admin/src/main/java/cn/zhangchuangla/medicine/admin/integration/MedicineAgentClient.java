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
 * 知识库 Agent 服务调用客户端。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineAgentClient {

    private static final String CREATE_PATH = "/knowledge_base";
    private static final String LOAD_PATH = "/knowledge_base/load";
    private static final String RELEASE_PATH = "/knowledge_base/release";

    private final KnowledgeBaseAiProperties properties;

    /**
     * 调用 Agent 服务创建知识库。
     */
    public void createKnowledgeBase(String knowledgeName, Integer embeddingDim, String description) {
        Assert.notEmpty(knowledgeName, "知识库名称不能为空");
        Assert.isPositive(embeddingDim, "向量维度必须大于0");

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

    private record CreateKnowledgeBasePayload(String knowledge_name, Integer embedding_dim, String description) {
    }

    private record CollectionPayload(String collection_name) {
    }
}
