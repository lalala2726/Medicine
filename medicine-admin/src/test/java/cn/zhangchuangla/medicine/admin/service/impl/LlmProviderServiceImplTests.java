package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.LlmProviderConnectivityClient;
import cn.zhangchuangla.medicine.admin.mapper.LlmProviderMapper;
import cn.zhangchuangla.medicine.admin.model.request.LlmProviderApiKeyUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.request.LlmProviderConnectivityTestRequest;
import cn.zhangchuangla.medicine.admin.model.request.LlmProviderCreateRequest;
import cn.zhangchuangla.medicine.admin.model.request.LlmProviderUpdateRequest;
import cn.zhangchuangla.medicine.common.core.config.JacksonConfig;
import cn.zhangchuangla.medicine.common.core.exception.ParamException;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.http.exception.HttpClientException;
import cn.zhangchuangla.medicine.common.http.model.HttpResult;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmProviderServiceImplTests {

    @Mock
    private LlmProviderMapper llmProviderMapper;

    @Mock
    private LlmProviderConnectivityClient llmProviderConnectivityClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LlmProviderServiceImpl llmProviderService;

    @Test
    void createProvider_ShouldReturnPersistedProvider() {
        LlmProviderCreateRequest request = new LlmProviderCreateRequest();
        request.setProviderName("OpenAI Custom");
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");

        when(llmProviderMapper.selectCount(any())).thenReturn(0L);
        when(llmProviderMapper.insert(any(LlmProvider.class))).thenAnswer(invocation -> {
            LlmProvider provider = invocation.getArgument(0);
            provider.setId(1L);
            return 1;
        });

        LlmProvider provider = llmProviderService.createProvider(request);

        assertEquals(1L, provider.getId());
        assertEquals("OpenAI Custom", provider.getProviderName());
        assertEquals("https://api.openai.com/v1", provider.getBaseUrl());

        ArgumentCaptor<LlmProvider> providerCaptor = ArgumentCaptor.forClass(LlmProvider.class);
        verify(llmProviderMapper).insert(providerCaptor.capture());
        assertEquals("OpenAI Custom", providerCaptor.getValue().getProviderName());
    }

    @Test
    void updateProvider_ShouldReturnUpdatedProvider() {
        LlmProvider existing = LlmProvider.builder()
                .id(1L)
                .providerName("Old Provider")
                .baseUrl("https://old.example.com/v1")
                .status(0)
                .sort(10)
                .apiKey("sk-old")
                .build();
        LlmProviderUpdateRequest request = new LlmProviderUpdateRequest();
        request.setId(1L);
        request.setProviderName("New Provider");
        request.setBaseUrl("https://new.example.com/v1");

        when(llmProviderMapper.selectById(1L)).thenReturn(existing);
        when(llmProviderMapper.selectCount(any())).thenReturn(0L);
        when(llmProviderMapper.updateById(any(LlmProvider.class))).thenReturn(1);

        LlmProvider provider = llmProviderService.updateProvider(request);

        assertEquals(1L, provider.getId());
        assertEquals("New Provider", provider.getProviderName());
        assertEquals("https://new.example.com/v1", provider.getBaseUrl());
        assertEquals("sk-old", provider.getApiKey());

        ArgumentCaptor<LlmProvider> providerCaptor = ArgumentCaptor.forClass(LlmProvider.class);
        verify(llmProviderMapper).updateById(providerCaptor.capture());
        assertEquals("sk-old", providerCaptor.getValue().getApiKey());
    }

    @Test
    void updateProviderApiKey_ShouldOnlyUpdateApiKey() {
        LlmProvider existing = LlmProvider.builder()
                .id(1L)
                .providerName("OpenAI")
                .apiKey("sk-old")
                .build();
        LlmProviderApiKeyUpdateRequest request = new LlmProviderApiKeyUpdateRequest();
        request.setId(1L);
        request.setApiKey("sk-new");

        when(llmProviderMapper.selectById(1L)).thenReturn(existing);
        when(llmProviderMapper.updateById(any(LlmProvider.class))).thenReturn(1);

        boolean result = llmProviderService.updateProviderApiKey(request);

        assertTrue(result);
        ArgumentCaptor<LlmProvider> providerCaptor = ArgumentCaptor.forClass(LlmProvider.class);
        verify(llmProviderMapper).updateById(providerCaptor.capture());
        assertEquals(1L, providerCaptor.getValue().getId());
        assertEquals("sk-new", providerCaptor.getValue().getApiKey());
    }

    @Test
    void updateProviderApiKey_WhenProviderMissing_ShouldThrowServiceException() {
        LlmProviderApiKeyUpdateRequest request = new LlmProviderApiKeyUpdateRequest();
        request.setId(1L);
        request.setApiKey("sk-new");
        when(llmProviderMapper.selectById(1L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> llmProviderService.updateProviderApiKey(request));

        assertEquals("提供商不存在", exception.getMessage());
    }

    @Test
    void updateProviderApiKey_WhenApiKeyBlank_ShouldThrowParamException() {
        LlmProvider existing = LlmProvider.builder()
                .id(1L)
                .providerName("OpenAI")
                .apiKey("sk-old")
                .build();
        LlmProviderApiKeyUpdateRequest request = new LlmProviderApiKeyUpdateRequest();
        request.setId(1L);
        request.setApiKey("   ");
        when(llmProviderMapper.selectById(1L)).thenReturn(existing);

        ParamException exception = assertThrows(ParamException.class,
                () -> llmProviderService.updateProviderApiKey(request));

        assertEquals("API Key不能为空", exception.getMessage());
    }

    @Test
    void updateProviderRequest_WhenJsonContainsApiKey_ShouldIgnoreUnknownProperty() throws Exception {
        String json = """
                {
                  "id": 1,
                  "providerName": "OpenAI",
                  "baseUrl": "https://api.openai.com/v1",
                  "apiKey": "sk-ignored",
                  "models": []
                }
                """;

        LlmProviderUpdateRequest request = new JacksonConfig().jsonMapper()
                .readValue(json, LlmProviderUpdateRequest.class);

        assertEquals(1L, request.getId());
        assertEquals("OpenAI", request.getProviderName());
        assertEquals("https://api.openai.com/v1", request.getBaseUrl());
    }

    @Test
    void deleteProvider_ShouldDeleteProviderOnly() {
        when(llmProviderMapper.selectById(1L)).thenReturn(LlmProvider.builder().id(1L).build());
        when(llmProviderMapper.deleteById(1L)).thenReturn(1);

        boolean result = llmProviderService.deleteProvider(1L);

        assertTrue(result);
        verify(llmProviderMapper).deleteById(1L);
    }

    @Test
    void getRequiredProvider_WhenProviderMissing_ShouldThrowServiceException() {
        when(llmProviderMapper.selectById(1L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> llmProviderService.getRequiredProvider(1L));

        assertEquals("提供商不存在", exception.getMessage());
    }

    @Test
    void testConnectivity_ShouldAppendModelsAndReturnSuccess() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://api.openai.com/v1/models", "sk-test"))
                .thenReturn(HttpResult.<String>builder()
                        .statusCode(200)
                        .body("{\"data\":[]}")
                        .build());

        var result = llmProviderService.testConnectivity(request);

        assertTrue(result.getSuccess());
        assertEquals(200, result.getHttpStatus());
        assertEquals("https://api.openai.com/v1/models", result.getEndpoint());
        assertEquals("连通成功", result.getMessage());
        verify(llmProviderConnectivityClient).getModels("https://api.openai.com/v1/models", "sk-test");
    }

    @Test
    void testConnectivity_ShouldTrimTrailingSlash() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1/");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://dashscope.aliyuncs.com/compatible-mode/v1/models",
                "sk-test")).thenReturn(HttpResult.<String>builder()
                .statusCode(200)
                .body("{\"data\":[]}")
                .build());

        var result = llmProviderService.testConnectivity(request);

        assertEquals("https://dashscope.aliyuncs.com/compatible-mode/v1/models", result.getEndpoint());
        verify(llmProviderConnectivityClient).getModels("https://dashscope.aliyuncs.com/compatible-mode/v1/models",
                "sk-test");
    }

    @Test
    void testConnectivity_ShouldKeepModelsSuffix() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://ark.cn-beijing.volces.com/api/v3/models");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://ark.cn-beijing.volces.com/api/v3/models", "sk-test"))
                .thenReturn(HttpResult.<String>builder()
                        .statusCode(200)
                        .body("{\"data\":[]}")
                        .build());

        var result = llmProviderService.testConnectivity(request);

        assertEquals("https://ark.cn-beijing.volces.com/api/v3/models", result.getEndpoint());
        verify(llmProviderConnectivityClient).getModels("https://ark.cn-beijing.volces.com/api/v3/models", "sk-test");
    }

    @Test
    void testConnectivity_WhenResponseIsNotOpenAiFormat_ShouldReturnFailure() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://api.openai.com/v1/models", "sk-test"))
                .thenReturn(HttpResult.<String>builder()
                        .statusCode(200)
                        .body("{\"object\":\"list\"}")
                        .build());

        var result = llmProviderService.testConnectivity(request);

        assertFalse(result.getSuccess());
        assertEquals("接口返回不符合 OpenAI 兼容格式", result.getMessage());
    }

    @Test
    void testConnectivity_WhenUnauthorized_ShouldMapMessage() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://api.openai.com/v1/models", "sk-test"))
                .thenReturn(HttpResult.<String>builder()
                        .statusCode(401)
                        .body("{\"error\":\"unauthorized\"}")
                        .build());

        var result = llmProviderService.testConnectivity(request);

        assertFalse(result.getSuccess());
        assertEquals("API Key 无效或已过期", result.getMessage());
    }

    @Test
    void testConnectivity_WhenForbidden_ShouldMapMessage() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://api.openai.com/v1/models", "sk-test"))
                .thenReturn(HttpResult.<String>builder()
                        .statusCode(403)
                        .body("{\"error\":\"forbidden\"}")
                        .build());

        var result = llmProviderService.testConnectivity(request);

        assertFalse(result.getSuccess());
        assertEquals("当前 API Key 无访问权限", result.getMessage());
    }

    @Test
    void testConnectivity_WhenNotFound_ShouldMapMessage() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://api.openai.com/v1/models", "sk-test"))
                .thenReturn(HttpResult.<String>builder()
                        .statusCode(404)
                        .body("{\"error\":\"not_found\"}")
                        .build());

        var result = llmProviderService.testConnectivity(request);

        assertFalse(result.getSuccess());
        assertEquals("BaseURL 不正确或目标服务不是 OpenAI 兼容接口", result.getMessage());
    }

    @Test
    void testConnectivity_WhenTooManyRequests_ShouldMapMessage() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://api.openai.com/v1/models", "sk-test"))
                .thenReturn(HttpResult.<String>builder()
                        .statusCode(429)
                        .body("{\"error\":\"rate_limited\"}")
                        .build());

        var result = llmProviderService.testConnectivity(request);

        assertFalse(result.getSuccess());
        assertEquals("请求受限或额度不足", result.getMessage());
    }

    @Test
    void testConnectivity_WhenServerError_ShouldReturnStatusMessage() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://api.openai.com/v1/models", "sk-test"))
                .thenReturn(HttpResult.<String>builder()
                        .statusCode(500)
                        .body("{\"error\":\"server_error\"}")
                        .build());

        var result = llmProviderService.testConnectivity(request);

        assertFalse(result.getSuccess());
        assertEquals("连通失败，HTTP 状态码: 500", result.getMessage());
    }

    @Test
    void testConnectivity_WhenHttpClientThrows_ShouldReturnNetworkFailure() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("https://api.openai.com/v1");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("https://api.openai.com/v1/models", "sk-test"))
                .thenThrow(new HttpClientException("Connect timed out"));

        var result = llmProviderService.testConnectivity(request);

        assertFalse(result.getSuccess());
        assertEquals("网络连接失败", result.getMessage());
        assertNull(result.getHttpStatus());
    }

    @Test
    void testConnectivity_WhenUrlInvalid_ShouldReturnInvalidBaseUrlMessage() {
        LlmProviderConnectivityTestRequest request = new LlmProviderConnectivityTestRequest();
        request.setBaseUrl("ht!tp://bad");
        request.setApiKey("sk-test");
        when(llmProviderConnectivityClient.getModels("ht!tp://bad/models", "sk-test"))
                .thenThrow(new HttpClientException("Invalid url: ht!tp://bad/models"));

        var result = llmProviderService.testConnectivity(request);

        assertFalse(result.getSuccess());
        assertEquals("BaseURL 格式不正确", result.getMessage());
        assertNull(result.getHttpStatus());
    }
}
