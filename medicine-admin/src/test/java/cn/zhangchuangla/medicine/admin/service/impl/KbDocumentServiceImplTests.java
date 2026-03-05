package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.admin.publisher.KnowledgeImportPublisher;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private KnowledgeImportPublisher knowledgeImportPublisher;

    private KbDocumentServiceImpl kbDocumentService;

    @BeforeEach
    void setUp() {
        RedisCache redisCache = new RedisCache(redisTemplate);
        kbDocumentService = spy(new KbDocumentServiceImpl(kbBaseService, redisCache, knowledgeImportPublisher));
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
    }

    @Test
    void handleImportResult_WhenLatest_ShouldUpdateStatusAndError() {
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
        assertEquals("parse failed", updated.getLastError());
        assertEquals("system", updated.getUpdateBy());
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
