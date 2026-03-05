package cn.zhangchuangla.medicine.admin.integration;

import cn.zhangchuangla.medicine.admin.config.KnowledgeBaseAiProperties;
import cn.zhangchuangla.medicine.common.core.exception.ParamException;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.JSONUtils;
import cn.zhangchuangla.medicine.common.http.exception.HttpClientException;
import cn.zhangchuangla.medicine.common.http.model.ClientRequest;
import cn.zhangchuangla.medicine.common.http.model.HttpResult;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MedicineAgentClientTests {

    @Test
    void createKnowledgeBase_WhenEmbeddingDimNotPowerOfTwo_ShouldThrowParamException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));

        ParamException exception = assertThrows(ParamException.class,
                () -> client.createKnowledgeBase("kb_123", 1000, "desc"));
        assertEquals("向量维度必须是2的幂", exception.getMessage());
        verify(client, never()).executePostRequest(anyString(), anyString());
    }

    @Test
    void createKnowledgeBase_ShouldSendCorrectRequest() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        doReturn(httpOk("{\"code\":200,\"message\":\"ok\"}"))
                .when(client)
                .executePostRequest(urlCaptor.capture(), bodyCaptor.capture());

        client.createKnowledgeBase("kb_123", 1024, "desc");

        assertEquals("http://localhost:8000/knowledge_base", urlCaptor.getValue());
        JsonObject bodyJson = JSONUtils.parseObject(bodyCaptor.getValue());
        assertEquals("kb_123", bodyJson.get("knowledge_name").getAsString());
        assertEquals(1024, bodyJson.get("embedding_dim").getAsInt());
        assertEquals("desc", bodyJson.get("description").getAsString());
    }

    @Test
    void createKnowledgeBase_WhenHttpStatusFailed_ShouldThrowException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doReturn(httpError("{\"code\":200,\"message\":\"ok\"}"))
                .when(client)
                .executePostRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.createKnowledgeBase("kb_123", 1024, "desc"));
    }

    @Test
    void createKnowledgeBase_WhenBodyCodeFailed_ShouldThrowException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doReturn(httpOk("{\"code\":500,\"message\":\"failed\"}"))
                .when(client)
                .executePostRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.createKnowledgeBase("kb_123", 1024, "desc"));
    }

    @Test
    void createKnowledgeBase_WhenBodyInvalid_ShouldThrowException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doReturn(httpOk("not-json"))
                .when(client)
                .executePostRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.createKnowledgeBase("kb_123", 1024, "desc"));
    }

    @Test
    void createKnowledgeBase_WhenNetworkError_ShouldThrowException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doThrow(new HttpClientException("network error"))
                .when(client)
                .executePostRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.createKnowledgeBase("kb_123", 1024, "desc"));
    }

    @Test
    void loadKnowledgeBase_ShouldSendCorrectRequest() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        doReturn(httpOk("{\"code\":200,\"message\":\"ok\"}"))
                .when(client)
                .executePostRequest(urlCaptor.capture(), bodyCaptor.capture());

        client.loadKnowledgeBase("kb_123");

        assertEquals("http://localhost:8000/knowledge_base/load", urlCaptor.getValue());
        JsonObject bodyJson = JSONUtils.parseObject(bodyCaptor.getValue());
        assertEquals("kb_123", bodyJson.get("collection_name").getAsString());
    }

    @Test
    void releaseKnowledgeBase_ShouldSendCorrectRequest() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        doReturn(httpOk("{\"code\":200,\"message\":\"ok\"}"))
                .when(client)
                .executePostRequest(urlCaptor.capture(), bodyCaptor.capture());

        client.releaseKnowledgeBase("kb_123");

        assertEquals("http://localhost:8000/knowledge_base/release", urlCaptor.getValue());
        JsonObject bodyJson = JSONUtils.parseObject(bodyCaptor.getValue());
        assertEquals("kb_123", bodyJson.get("collection_name").getAsString());
    }

    @Test
    void loadKnowledgeBase_WhenHttpStatusFailed_ShouldThrowException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doReturn(httpError("{\"code\":200,\"message\":\"ok\"}"))
                .when(client)
                .executePostRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.loadKnowledgeBase("kb_123"));
    }

    @Test
    void releaseKnowledgeBase_WhenNetworkError_ShouldThrowException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doThrow(new HttpClientException("network error"))
                .when(client)
                .executePostRequest(anyString(), anyString());

        assertThrows(ServiceException.class, () -> client.releaseKnowledgeBase("kb_123"));
    }

    @Test
    void listDocumentChunks_ShouldPaginateAndAggregateRows() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doReturn(httpOk("{\"code\":200,\"message\":\"ok\",\"data\":{\"rows\":[{\"id\":900001,\"document_id\":1001,\"chunk_index\":1,\"content\":\"A\",\"char_count\":1}],\"total\":2,\"page_num\":1,\"page_size\":50,\"has_next\":true}}"))
                .doReturn(httpOk("{\"code\":200,\"message\":\"ok\",\"data\":{\"rows\":[{\"id\":900002,\"document_id\":1001,\"chunk_index\":2,\"content\":\"B\",\"char_count\":1}],\"total\":2,\"page_num\":2,\"page_size\":50,\"has_next\":false}}"))
                .when(client)
                .executeGetRequest(any(ClientRequest.class));

        List<MedicineAgentClient.DocumentChunkRow> rows = client.listDocumentChunks("kb_123", 1001L);

        assertEquals(2, rows.size());
        assertEquals(900001L, rows.get(0).getId());
        assertEquals(900002L, rows.get(1).getId());

        ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(client, times(2)).executeGetRequest(requestCaptor.capture());
        List<ClientRequest> requests = requestCaptor.getAllValues();
        assertEquals("kb_123", requests.get(0).getUrl().queryParameter("knowledge_name"));
        assertEquals("1001", requests.get(0).getUrl().queryParameter("document_id"));
        assertEquals("1", requests.get(0).getUrl().queryParameter("page"));
        assertEquals("50", requests.get(0).getUrl().queryParameter("page_size"));
        assertEquals("2", requests.get(1).getUrl().queryParameter("page"));
    }

    @Test
    void listDocumentChunks_WhenBodyCodeFailed_ShouldThrowException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doReturn(httpOk("{\"code\":500,\"message\":\"failed\"}"))
                .when(client)
                .executeGetRequest(any(ClientRequest.class));

        assertThrows(ServiceException.class, () -> client.listDocumentChunks("kb_123", 1001L));
    }

    @Test
    void listDocumentChunks_WhenBodyInvalid_ShouldThrowException() {
        MedicineAgentClient client = spy(newClient("http://localhost:8000"));
        doReturn(httpOk("not-json"))
                .when(client)
                .executeGetRequest(any(ClientRequest.class));

        assertThrows(ServiceException.class, () -> client.listDocumentChunks("kb_123", 1001L));
    }

    @Test
    void buildChunkListRequest_ShouldContainExpectedQueryParameters() {
        MedicineAgentClient client = newClient("http://localhost:8000");
        ClientRequest request = client.buildChunkListRequest(
                "http://localhost:8000/knowledge_base/document/chunks/list", "kb_123", 1001L, 3, 50);

        assertEquals("kb_123", request.getUrl().queryParameter("knowledge_name"));
        assertEquals("1001", request.getUrl().queryParameter("document_id"));
        assertEquals("3", request.getUrl().queryParameter("page"));
        assertEquals("50", request.getUrl().queryParameter("page_size"));
    }

    private HttpResult<String> httpOk(String body) {
        return HttpResult.<String>builder()
                .statusCode(200)
                .body(body)
                .build();
    }

    private HttpResult<String> httpError(String body) {
        return HttpResult.<String>builder()
                .statusCode(500)
                .body(body)
                .build();
    }

    private MedicineAgentClient newClient(String baseUrl) {
        KnowledgeBaseAiProperties properties = new KnowledgeBaseAiProperties();
        properties.setBaseUrl(baseUrl);
        return new MedicineAgentClient(properties);
    }
}
