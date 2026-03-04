package cn.zhangchuangla.medicine.admin.integration;

import cn.zhangchuangla.medicine.admin.config.KnowledgeBaseAiProperties;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.JSONUtils;
import cn.zhangchuangla.medicine.common.http.exception.HttpClientException;
import cn.zhangchuangla.medicine.common.http.model.HttpResult;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KnowledgeBaseAiClientTests {

    @Test
    void createKnowledgeBase_ShouldSendCorrectRequest() {
        KnowledgeBaseAiClient client = spy(newClient("http://localhost:8000"));
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        doReturn(HttpResult.<String>builder()
                .statusCode(200)
                .body("{\"code\":200,\"message\":\"ok\"}")
                .build())
                .when(client)
                .executeCreateRequest(urlCaptor.capture(), bodyCaptor.capture());

        client.createKnowledgeBase("kb_123", 1024, "desc");

        assertEquals("http://localhost:8000/knowledge_base", urlCaptor.getValue());
        JsonObject bodyJson = JSONUtils.parseObject(bodyCaptor.getValue());
        assertEquals("kb_123", bodyJson.get("knowledge_name").getAsString());
        assertEquals(1024, bodyJson.get("embedding_dim").getAsInt());
        assertEquals("desc", bodyJson.get("description").getAsString());
    }

    @Test
    void createKnowledgeBase_WhenHttpStatusFailed_ShouldThrowException() {
        KnowledgeBaseAiClient client = spy(newClient("http://localhost:8000"));
        doReturn(HttpResult.<String>builder()
                .statusCode(500)
                .body("{\"code\":200,\"message\":\"ok\"}")
                .build())
                .when(client)
                .executeCreateRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.createKnowledgeBase("kb_123", 1024, "desc"));
    }

    @Test
    void createKnowledgeBase_WhenBodyCodeFailed_ShouldThrowException() {
        KnowledgeBaseAiClient client = spy(newClient("http://localhost:8000"));
        doReturn(HttpResult.<String>builder()
                .statusCode(200)
                .body("{\"code\":500,\"message\":\"failed\"}")
                .build())
                .when(client)
                .executeCreateRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.createKnowledgeBase("kb_123", 1024, "desc"));
    }

    @Test
    void createKnowledgeBase_WhenBodyInvalid_ShouldThrowException() {
        KnowledgeBaseAiClient client = spy(newClient("http://localhost:8000"));
        doReturn(HttpResult.<String>builder()
                .statusCode(200)
                .body("not-json")
                .build())
                .when(client)
                .executeCreateRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.createKnowledgeBase("kb_123", 1024, "desc"));
    }

    @Test
    void createKnowledgeBase_WhenNetworkError_ShouldThrowException() {
        KnowledgeBaseAiClient client = spy(newClient("http://localhost:8000"));
        doThrow(new HttpClientException("network error"))
                .when(client)
                .executeCreateRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.createKnowledgeBase("kb_123", 1024, "desc"));
    }

    private KnowledgeBaseAiClient newClient(String baseUrl) {
        KnowledgeBaseAiProperties properties = new KnowledgeBaseAiProperties();
        properties.setBaseUrl(baseUrl);
        return new KnowledgeBaseAiClient(properties);
    }
}
