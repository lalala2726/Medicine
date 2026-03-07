package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.MedicineAgentClient;
import cn.zhangchuangla.medicine.admin.mapper.KbDocumentMapper;
import cn.zhangchuangla.medicine.admin.model.request.DocumentDeleteRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.admin.publisher.KnowledgePublisher;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import cn.zhangchuangla.medicine.model.enums.KbDocumentStageEnum;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportDocumentMessage;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KbDocumentServiceImplTests {

    @Mock
    private KbBaseService kbBaseService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private KnowledgePublisher knowledgePublisher;

    @Mock
    private MedicineAgentClient medicineAgentClient;

    @Mock
    private KbDocumentMapper kbDocumentMapper;

    @Mock
    private KbDocumentChunkService kbDocumentChunkService;

    private KbDocumentServiceImpl kbDocumentService;

    @BeforeEach
    void setUp() {
        RedisCache redisCache = new RedisCache(redisTemplate);
        kbDocumentService = spy(new KbDocumentServiceImpl(
                kbBaseService, redisCache, knowledgePublisher, medicineAgentClient, kbDocumentChunkService));
        ReflectionTestUtils.setField(kbDocumentService, "baseMapper", kbDocumentMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void listDocument_WhenKnowledgeBaseNotFound_ShouldThrowException() {
        DocumentListRequest request = new DocumentListRequest();
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> kbDocumentService.listDocument(1L, request));

        assertEquals("知识库不存在", ex.getMessage());
        verify(kbDocumentMapper, never()).listDocument(any(), anyLong(), any());
    }

    @Test
    void importDocument_WhenKnowledgeBaseNotFound_ShouldThrowException() {
        KnowledgeBaseImportRequest request = newImportRequest();
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> kbDocumentService.importDocument(request));

        assertEquals("知识库不存在", ex.getMessage());
        verify(kbDocumentService, never()).save(any(KbDocument.class));
        verify(knowledgePublisher, never()).publishImportDocument(any(KnowledgeImportDocumentMessage.class));
    }

    @Test
    void importDocument_ShouldSaveDocumentAndPublishImportDocument() {
        KnowledgeBaseImportRequest request = newImportRequest();
        KbBase kbBase = newKbBase();
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(kbBase);
        doReturn("admin").when(kbDocumentService).getUsername();
        doAnswer(invocation -> {
            KbDocument doc = invocation.getArgument(0);
            doc.setId(1001L);
            return true;
        }).when(kbDocumentService).save(any(KbDocument.class));
        when(valueOperations.increment("kb:latest:drug_faq:1001")).thenReturn(1L);
        when(redisTemplate.expire("kb:latest:drug_faq:1001", 7L, TimeUnit.DAYS)).thenReturn(true);

        kbDocumentService.importDocument(request);

        ArgumentCaptor<KbDocument> documentCaptor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).save(documentCaptor.capture());
        KbDocument savedDocument = documentCaptor.getValue();
        assertEquals("知识库导入说明.pdf", savedDocument.getFileName());
        assertEquals("https://example.com/files/asset-001", savedDocument.getFileUrl());

        ArgumentCaptor<KnowledgeImportDocumentMessage> msgCaptor = ArgumentCaptor.forClass(KnowledgeImportDocumentMessage.class);
        verify(knowledgePublisher).publishImportDocument(msgCaptor.capture());
        KnowledgeImportDocumentMessage message = msgCaptor.getValue();
        assertEquals("knowledge_import_command", message.getMessage_type());
        assertEquals("drug_faq:1001", message.getBiz_key());
        assertEquals(1L, message.getVersion());
        assertEquals("drug_faq", message.getKnowledge_name());
        assertEquals(1001L, message.getDocument_id());
        assertEquals("https://example.com/files/asset-001", message.getFile_url());
        assertEquals("text-embedding-3-large", message.getEmbedding_model());
        assertEquals("character", message.getChunk_strategy());
        assertEquals(500, message.getChunk_size());
        assertEquals(100, message.getToken_size());
    }

    @Test
    void importDocument_WhenPublishFailed_ShouldMarkDocumentFailedAndThrow() {
        KnowledgeBaseImportRequest request = newImportRequest();
        KbBase kbBase = newKbBase();
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(kbBase);
        doReturn("admin").when(kbDocumentService).getUsername();
        doAnswer(invocation -> {
            KbDocument doc = invocation.getArgument(0);
            doc.setId(1001L);
            return true;
        }).when(kbDocumentService).save(any(KbDocument.class));
        when(valueOperations.increment("kb:latest:drug_faq:1001")).thenReturn(1L);
        when(redisTemplate.expire("kb:latest:drug_faq:1001", 7L, TimeUnit.DAYS)).thenReturn(true);
        doThrow(new ServiceException("mq error")).when(knowledgePublisher)
                .publishImportDocument(any(KnowledgeImportDocumentMessage.class));
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        assertThrows(ServiceException.class, () -> kbDocumentService.importDocument(request));

        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals(KbDocumentStageEnum.FAILED.getCode(), updated.getStage());
        assertEquals("mq error", updated.getLastError());
    }

    @Test
    void handleImportResult_WhenStaleVersion_ShouldDrop() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-1")
                .biz_key("drug_faq:1001")
                .version(1L)
                .document_id(1001L)
                .stage(KbDocumentStageEnum.COMPLETED.getCode())
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);

        kbDocumentService.handleImportResult(message);

        verify(kbDocumentService, never()).getById(anyLong());
        verify(kbDocumentService, never()).updateById(any(KbDocument.class));
        verify(knowledgePublisher, never()).publishImportChunkUpdate(any(KnowledgeImportResultMessage.class));
    }

    @Test
    void handleImportResult_WhenLatestFailed_ShouldUpdateStatusAndError() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-2")
                .biz_key("drug_faq:1001")
                .version(2L)
                .document_id(1001L)
                .stage(KbDocumentStageEnum.FAILED.getCode())
                .message("parse failed")
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStage(KbDocumentStageEnum.PENDING.getCode());
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        kbDocumentService.handleImportResult(message);

        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals(KbDocumentStageEnum.FAILED.getCode(), updated.getStage());
        assertEquals("parse failed", updated.getLastError());
        assertEquals("system", updated.getUpdateBy());
        verify(knowledgePublisher, never()).publishImportChunkUpdate(any(KnowledgeImportResultMessage.class));
    }

    @Test
    void handleImportResult_WhenStageUnsupported_ShouldIgnore() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-2-0")
                .biz_key("drug_faq:1001")
                .version(2L)
                .document_id(1001L)
                .stage(KbDocumentStageEnum.INSERTING.getCode())
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStage(KbDocumentStageEnum.PENDING.getCode());
        doReturn(existing).when(kbDocumentService).getById(1001L);

        kbDocumentService.handleImportResult(message);

        verify(kbDocumentService, never()).updateById(any(KbDocument.class));
        verify(knowledgePublisher, never()).publishImportChunkUpdate(any(KnowledgeImportResultMessage.class));
    }

    @Test
    void handleImportResult_WhenProcessing_ShouldUpdateStatusOnly() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-2-1")
                .biz_key("drug_faq:1001")
                .version(2L)
                .document_id(1001L)
                .stage(KbDocumentStageEnum.PROCESSING.getCode())
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStage(KbDocumentStageEnum.PENDING.getCode());
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        kbDocumentService.handleImportResult(message);

        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals(KbDocumentStageEnum.PROCESSING.getCode(), updated.getStage());
        assertNull(updated.getLastError());
    }

    @Test
    void handleImportResult_WhenLatestCompleted_ShouldSetInsertingAndPublishChunkUpdate() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-3")
                .biz_key("drug_faq:1001")
                .version(2L)
                .document_id(1001L)
                .stage(KbDocumentStageEnum.COMPLETED.getCode())
                .knowledge_name("drug_faq")
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(2L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStage(KbDocumentStageEnum.PENDING.getCode());
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        kbDocumentService.handleImportResult(message);

        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals(KbDocumentStageEnum.INSERTING.getCode(), updated.getStage());
        assertNull(updated.getLastError());
        verify(knowledgePublisher).publishImportChunkUpdate(message);
    }

    @Test
    void deleteDocuments_ShouldCallAgentAndRemoveLocalData() {
        DocumentDeleteRequest request = new DocumentDeleteRequest();
        request.setDocumentIds(List.of(1001L, 1002L, 1001L));

        KbBase kbBase = newKbBase();
        kbBase.setId(1L);
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(kbBase);
        doReturn(List.of(newDocument(1001L, 1L), newDocument(1002L, 1L)))
                .when(kbDocumentService).listByIds(List.of(1001L, 1002L));
        doReturn(true).when(kbDocumentService).removeByIds(List.of(1001L, 1002L));
        when(kbDocumentChunkService.removeByDocumentIds(List.of(1001L, 1002L))).thenReturn(true);

        boolean result = kbDocumentService.deleteDocuments(request);

        assertTrue(result);
        verify(medicineAgentClient).deleteDocuments("drug_faq", List.of(1001L, 1002L));
        verify(kbDocumentChunkService).removeByDocumentIds(List.of(1001L, 1002L));
        verify(kbDocumentService).removeByIds(List.of(1001L, 1002L));
    }

    @Test
    void handleChunkUpdateResult_WhenSyncSuccess_ShouldReplaceChunksAndMarkCompleted() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-4")
                .biz_key("drug_faq:1001")
                .version(3L)
                .document_id(1001L)
                .knowledge_name("drug_faq")
                .stage(KbDocumentStageEnum.COMPLETED.getCode())
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(3L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStage(KbDocumentStageEnum.INSERTING.getCode());
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));

        MedicineAgentClient.DocumentChunkRow row = new MedicineAgentClient.DocumentChunkRow();
        row.setId(900001L);
        row.setDocument_id(1001L);
        row.setChunk_index(1);
        row.setContent("切片内容");
        row.setChar_count(128);
        row.setStatus(1);
        when(medicineAgentClient.listDocumentChunks("drug_faq", 1001L)).thenReturn(List.of(row));

        kbDocumentService.handleChunkUpdateResult(message);

        ArgumentCaptor<List> chunksCaptor = ArgumentCaptor.forClass(List.class);
        verify(kbDocumentChunkService).replaceByDocumentId(eq(1001L), chunksCaptor.capture());
        List<KbDocumentChunk> chunks = chunksCaptor.getValue();
        assertEquals(1, chunks.size());
        assertEquals(1, chunks.get(0).getStatus());
        assertEquals(1001L, chunks.get(0).getDocumentId());
        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals(KbDocumentStageEnum.COMPLETED.getCode(), updated.getStage());
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
                .stage(KbDocumentStageEnum.COMPLETED.getCode())
                .build();
        when(valueOperations.get("kb:latest:drug_faq:1001")).thenReturn(3L);
        KbDocument existing = new KbDocument();
        existing.setId(1001L);
        existing.setStage(KbDocumentStageEnum.INSERTING.getCode());
        doReturn(existing).when(kbDocumentService).getById(1001L);
        doReturn(true).when(kbDocumentService).updateById(any(KbDocument.class));
        doThrow(new ServiceException("sync error")).when(medicineAgentClient).listDocumentChunks("drug_faq", 1001L);

        kbDocumentService.handleChunkUpdateResult(message);

        verify(medicineAgentClient, times(3)).listDocumentChunks("drug_faq", 1001L);
        ArgumentCaptor<KbDocument> captor = ArgumentCaptor.forClass(KbDocument.class);
        verify(kbDocumentService).updateById(captor.capture());
        KbDocument updated = captor.getValue();
        assertEquals(KbDocumentStageEnum.FAILED.getCode(), updated.getStage());
        assertEquals("sync error", updated.getLastError());
    }

    private KnowledgeBaseImportRequest newImportRequest() {
        KnowledgeBaseImportRequest request = new KnowledgeBaseImportRequest();
        request.setKnowledgeBaseId(1L);
        request.setFileDetails(List.of(KnowledgeBaseImportRequest.FileDetail.builder()
                .fileName("知识库导入说明.pdf")
                .fileUrl("https://example.com/files/asset-001")
                .build()));
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

    private KbDocument newDocument(Long id, Long knowledgeBaseId) {
        KbDocument document = new KbDocument();
        document.setId(id);
        document.setKnowledgeBaseId(knowledgeBaseId);
        return document;
    }
}
