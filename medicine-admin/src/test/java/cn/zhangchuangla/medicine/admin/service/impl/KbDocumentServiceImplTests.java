package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.MedicineAgentClient;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.admin.publisher.KnowledgeImportPublisher;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportCommandMessage;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyList;

@ExtendWith(MockitoExtension.class)
class KbDocumentServiceImplTests {

    @Mock
    private KbBaseService kbBaseService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private KnowledgeImportPublisher knowledgeImportPublisher;

    @Mock
    private MedicineAgentClient medicineAgentClient;

    @Mock
    private KbDocumentChunkService kbDocumentChunkService;

    private KbDocumentServiceImpl kbDocumentService;

    @BeforeEach
    void setUp() {
        RedisCache redisCache = new RedisCache(redisTemplate);
        kbDocumentService = spy(new KbDocumentServiceImpl(
                kbBaseService, redisCache, knowledgeImportPublisher, medicineAgentClient, kbDocumentChunkService));
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void importKnowledge_WhenKnowledgeBaseNotFound_ShouldThrowException() {
        KnowledgeBaseImportRequest request = newImportRequest();
        doReturn(null).when(kbDocumentService).findKnowledgeBaseByName("drug_faq");

        ServiceException ex = assertThrows(ServiceException.class, () -> kbDocumentService.importKnowledge(request));

        assertEquals("知识库不存在", ex.getMessage());
        verify(kbDocumentService, never()).save(any(KbDocument.class));
        verify(knowledgeImportPublisher, never()).publishCommand(any(KnowledgeImportCommandMessage.class));
    }

    @Test
    void importKnowledge_ShouldSaveDocumentAndPublishCommand() {
        KnowledgeBaseImportRequest request = newImportRequest();
        KbBase kbBase = newKbBase();
        doReturn(kbBase).when(kbDocumentService).findKnowledgeBaseByName("drug_faq");
        doReturn("admin").when(kbDocumentService).getUsername();
        doAnswer(invocation -> {
            KbDocument doc = invocation.getArgument(0);
            doc.setId(1001L);
            return true;
        }).when(kbDocumentService).save(any(KbDocument.class));
        when(valueOperations.increment("kb:latest:drug_faq:1001")).thenReturn(1L);
        when(redisTemplate.expire("kb:latest:drug_faq:1001", 7L, TimeUnit.DAYS)).thenReturn(true);

        kbDocumentService.importKnowledge(request);

        ArgumentCaptor<KnowledgeImportCommandMessage> msgCaptor = ArgumentCaptor.forClass(KnowledgeImportCommandMessage.class);
        verify(knowledgeImportPublisher).publishCommand(msgCaptor.capture());
        KnowledgeImportCommandMessage message = msgCaptor.getValue();
        assertEquals("knowledge_import_command", message.getMessage_type());
        assertEquals("drug_faq:1001", message.getBiz_key());
        assertEquals(1L, message.getVersion());
        assertEquals("drug_faq", message.getKnowledge_name());
        assertEquals(1001L, message.getDocument_id());
        assertEquals("https://example.com/docs/guide.pdf", message.getFile_url());
        assertEquals("text-embedding-3-large", message.getEmbedding_model());
        assertEquals("character", message.getChunk_strategy());
        assertEquals(500, message.getChunk_size());
        assertEquals(100, message.getToken_size());
    }

    @Test
    void importKnowledge_WhenPublishFailed_ShouldMarkDocumentFailedAndThrow() {
        KnowledgeBaseImportRequest request = newImportRequest();
        KbBase kbBase = newKbBase();
        doReturn(kbBase).when(kbDocumentService).findKnowledgeBaseByName("drug_faq");
        doReturn("admin").when(kbDocumentService).getUsername();
        doAnswer(invocation -> {
            KbDocument doc = invocation.getArgument(0);
            doc.setId(1001L);
            return true;
        }).when(kbDocumentService).save(any(KbDocument.class));
        when(valueOperations.increment("kb:latest:drug_faq:1001")).thenReturn(1L);
        when(redisTemplate.expire("kb:latest:drug_faq:1001", 7L, TimeUnit.DAYS)).thenReturn(true);
        doThrow(new ServiceException("mq error")).when(knowledgeImportPublisher)
                .publishCommand(any(KnowledgeImportCommandMessage.class));
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        assertThrows(ServiceException.class, () -> kbDocumentService.importKnowledge(request));

        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals("FAILED", updated.getStatus());
        assertEquals("END", updated.getStageDetail());
        assertEquals("mq error", updated.getLastError());
    }

    @Test
    void handleImportResult_WhenStaleVersion_ShouldDrop() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-1")
                .biz_key("drug_faq:1001")
                .version(1L)
                .document_id(1001L)
                .stage("COMPLETED")
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);

        kbDocumentService.handleImportResult(message);

        verify(kbDocumentService, never()).getById(anyLong());
        verify(kbDocumentService, never()).updateById(any(KbDocument.class));
        verify(knowledgeImportPublisher, never()).publishChunkUpdate(any(KnowledgeImportResultMessage.class));
    }

    @Test
    void handleImportResult_WhenLatestFailed_ShouldUpdateStatusAndError() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-2")
                .biz_key("drug_faq:1001")
                .version(2L)
                .document_id(1001L)
                .stage("failed")
                .message("parse failed")
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStatus("PENDING");
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        kbDocumentService.handleImportResult(message);

        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals("FAILED", updated.getStatus());
        assertEquals("END", updated.getStageDetail());
        assertEquals("parse failed", updated.getLastError());
        assertEquals("system", updated.getUpdateBy());
        verify(knowledgeImportPublisher, never()).publishChunkUpdate(any(KnowledgeImportResultMessage.class));
    }

    @Test
    void handleImportResult_WhenProcessing_ShouldStoreNormalizedStageDetail() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-2-1")
                .biz_key("drug_faq:1001")
                .version(2L)
                .document_id(1001L)
                .stage("processing")
                .stage_detail("chunking")
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStatus("PENDING");
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        kbDocumentService.handleImportResult(message);

        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals("PROCESSING", updated.getStatus());
        assertEquals("CHUNKING", updated.getStageDetail());
        assertNull(updated.getLastError());
    }

    @Test
    void handleImportResult_WhenLatestCompleted_ShouldSetInsertingAndPublishChunkUpdate() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-3")
                .biz_key("drug_faq:1001")
                .version(2L)
                .document_id(1001L)
                .stage("COMPLETED")
                .knowledge_name("drug_faq")
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStatus("PENDING");
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        kbDocumentService.handleImportResult(message);

        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals("INSERTING", updated.getStatus());
        assertEquals("INSERTING", updated.getStageDetail());
        assertNull(updated.getLastError());
        verify(knowledgeImportPublisher).publishChunkUpdate(message);
    }

    @Test
    void handleChunkUpdateResult_WhenSyncSuccess_ShouldReplaceChunksAndMarkCompleted() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-4")
                .biz_key("drug_faq:1001")
                .version(3L)
                .document_id(1001L)
                .knowledge_name("drug_faq")
                .stage("COMPLETED")
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(3L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStatus("INSERTING");
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        MedicineAgentClient.DocumentChunkRow row = new MedicineAgentClient.DocumentChunkRow();
        row.setId(900001L);
        row.setDocument_id(1001L);
        row.setChunk_index(1);
        row.setContent("切片内容");
        row.setChar_count(128);
        when(medicineAgentClient.listDocumentChunks("drug_faq", 1001L)).thenReturn(List.of(row));

        kbDocumentService.handleChunkUpdateResult(message);

        verify(kbDocumentChunkService).replaceByDocumentId(eq("1001"), anyList());
        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals("COMPLETED", updated.getStatus());
        assertEquals("END", updated.getStageDetail());
        assertNull(updated.getLastError());
    }

    @Test
    void handleChunkUpdateResult_WhenSyncFailed_ShouldRetryAndMarkFailed() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-5")
                .biz_key("drug_faq:1001")
                .version(3L)
                .document_id(1001L)
                .knowledge_name("drug_faq")
                .stage("COMPLETED")
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(3L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStatus("INSERTING");
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));
        doThrow(new ServiceException("sync error")).when(medicineAgentClient).listDocumentChunks("drug_faq", 1001L);

        kbDocumentService.handleChunkUpdateResult(message);

        verify(medicineAgentClient, times(3)).listDocumentChunks("drug_faq", 1001L);
        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals("FAILED", updated.getStatus());
        assertEquals("INSERTING", updated.getStageDetail());
        assertEquals("sync error", updated.getLastError());
    }

    private KnowledgeBaseImportRequest newImportRequest() {
        KnowledgeBaseImportRequest request = new KnowledgeBaseImportRequest();
        request.setKnowledgeName("drug_faq");
        request.setFileUrls(List.of("https://example.com/docs/guide.pdf"));
        request.setChunkStrategy("character");
        request.setChunkSize(500);
        request.setTokenSize(100);
        return request;
    }

    private KbBase newKbBase() {
        KbBase kbBase = new KbBase();
        kbBase.setId(1L);
        kbBase.setKnowledgeName("drug_faq");
        kbBase.setEmbeddingModel("text-embedding-3-large");
        return kbBase;
    }
}
