package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.KbDocumentChunkMapper;
import cn.zhangchuangla.medicine.admin.mapper.KbDocumentMapper;
import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkListRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkUpdateContentRequest;
import cn.zhangchuangla.medicine.admin.publisher.KnowledgeChunkRebuildPublisher;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkHistoryService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunkHistory;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkRebuildCommandMessage;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkRebuildResultMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KbDocumentChunkServiceImplTests {

    @Mock
    private KbDocumentChunkMapper kbDocumentChunkMapper;

    @Mock
    private KbDocumentMapper kbDocumentMapper;

    @Mock
    private KbBaseService kbBaseService;

    @Mock
    private KbDocumentChunkHistoryService kbDocumentChunkHistoryService;

    @Mock
    private KnowledgeChunkRebuildPublisher knowledgeChunkRebuildPublisher;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private PlatformTransactionManager transactionManager;

    private KbDocumentChunkServiceImpl kbDocumentChunkService;

    @BeforeEach
    void setUp() {
        RedisCache redisCache = new RedisCache(redisTemplate);
        kbDocumentChunkService = new KbDocumentChunkServiceImpl(
                kbDocumentMapper,
                kbBaseService,
                kbDocumentChunkHistoryService,
                redisCache,
                knowledgeChunkRebuildPublisher,
                transactionManager
        );
        ReflectionTestUtils.setField(kbDocumentChunkService, "baseMapper", kbDocumentChunkMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void listDocumentChunk_ShouldQueryByDocumentId() {
        DocumentChunkListRequest request = new DocumentChunkListRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        KbDocument document = new KbDocument();
        document.setId(1001L);
        when(kbDocumentMapper.selectById(1001L)).thenReturn(document);

        Page<KbDocumentChunk> page = new Page<>(1, 10, 1);
        when(kbDocumentChunkMapper.listDocumentChunk(any(Page.class), eq(1001L), eq(request))).thenReturn(page);

        Page<KbDocumentChunk> result = kbDocumentChunkService.listDocumentChunk(1001L, request);

        assertSame(page, result);
        verify(kbDocumentMapper).selectById(1001L);
        verify(kbDocumentChunkMapper).listDocumentChunk(any(Page.class), eq(1001L), eq(request));
    }

    @Test
    void getDocumentChunkById_WhenMissing_ShouldThrow() {
        when(kbDocumentChunkMapper.selectById(2001L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> kbDocumentChunkService.getDocumentChunkById(2001L));

        assertEquals("文档切片不存在", ex.getMessage());
    }

    @Test
    void updateDocumentChunkContent_ShouldPersistHistoryAndPublishCommand() {
        DocumentChunkUpdateContentRequest request = new DocumentChunkUpdateContentRequest();
        request.setId(2001L);
        request.setContent("  新的切片内容  ");

        when(kbDocumentChunkMapper.selectById(2001L)).thenReturn(newChunk("旧的切片内容"));
        when(kbDocumentMapper.selectById(1001L)).thenReturn(newDocument());
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(newKbBase());
        when(kbDocumentChunkMapper.updateById(any(KbDocumentChunk.class))).thenReturn(1);
        when(kbDocumentChunkHistoryService.save(any(KbDocumentChunkHistory.class))).thenReturn(true);
        when(valueOperations.increment("kb:chunk_edit:latest_version:900001")).thenReturn(3L);
        when(redisTemplate.expire("kb:chunk_edit:latest_version:900001", 7L, TimeUnit.DAYS)).thenReturn(true);

        boolean result = kbDocumentChunkService.updateDocumentChunkContent(request);

        assertTrue(result);
        ArgumentCaptor<KbDocumentChunk> chunkCaptor = ArgumentCaptor.forClass(KbDocumentChunk.class);
        verify(kbDocumentChunkMapper).updateById(chunkCaptor.capture());
        KbDocumentChunk updated = chunkCaptor.getValue();
        assertEquals(2001L, updated.getId());
        assertEquals("新的切片内容", updated.getContent());
        assertEquals(6, updated.getCharCount());
        assertEquals("PENDING", updated.getEditStatus());

        ArgumentCaptor<KbDocumentChunkHistory> historyCaptor = ArgumentCaptor.forClass(KbDocumentChunkHistory.class);
        verify(kbDocumentChunkHistoryService).save(historyCaptor.capture());
        KbDocumentChunkHistory history = historyCaptor.getValue();
        assertEquals(1001L, history.getDocumentId());
        assertEquals(2001L, history.getChunkId());
        assertEquals("drug_faq", history.getKnowledgeName());
        assertEquals(900001L, history.getVectorId());
        assertEquals("旧的切片内容", history.getOldContent());

        ArgumentCaptor<KnowledgeChunkRebuildCommandMessage> messageCaptor =
                ArgumentCaptor.forClass(KnowledgeChunkRebuildCommandMessage.class);
        verify(knowledgeChunkRebuildPublisher).publishCommand(messageCaptor.capture());
        KnowledgeChunkRebuildCommandMessage message = messageCaptor.getValue();
        assertEquals("knowledge_chunk_rebuild_command", message.getMessage_type());
        assertEquals("drug_faq", message.getKnowledge_name());
        assertEquals(1001L, message.getDocument_id());
        assertEquals(900001L, message.getVector_id());
        assertEquals(3L, message.getVersion());
        assertEquals("新的切片内容", message.getContent());
        assertEquals("text-embedding-v4", message.getEmbedding_model());
    }

    @Test
    void updateDocumentChunkContent_WhenEditPending_ShouldThrow() {
        DocumentChunkUpdateContentRequest request = new DocumentChunkUpdateContentRequest();
        request.setId(2001L);
        request.setContent("新的切片内容");

        KbDocumentChunk chunk = newChunk("旧的切片内容");
        chunk.setEditStatus("PENDING");
        when(kbDocumentChunkMapper.selectById(2001L)).thenReturn(chunk);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> kbDocumentChunkService.updateDocumentChunkContent(request));

        assertEquals("当前切片已提交修改，必须等待完成后才能继续修改", ex.getMessage());
        verify(kbDocumentChunkMapper, never()).updateById(any(KbDocumentChunk.class));
        verify(kbDocumentChunkHistoryService, never()).save(any(KbDocumentChunkHistory.class));
        verify(knowledgeChunkRebuildPublisher, never()).publishCommand(any(KnowledgeChunkRebuildCommandMessage.class));
        verify(valueOperations, never()).increment(any());
    }

    @Test
    void updateDocumentChunkContent_WhenContentUnchanged_ShouldSkipHistoryRedisAndMq() {
        DocumentChunkUpdateContentRequest request = new DocumentChunkUpdateContentRequest();
        request.setId(2001L);
        request.setContent("  相同内容 ");
        when(kbDocumentChunkMapper.selectById(2001L)).thenReturn(newChunk("相同内容"));

        boolean result = kbDocumentChunkService.updateDocumentChunkContent(request);

        assertTrue(result);
        verify(kbDocumentChunkMapper, never()).updateById(any(KbDocumentChunk.class));
        verify(kbDocumentChunkHistoryService, never()).save(any(KbDocumentChunkHistory.class));
        verify(knowledgeChunkRebuildPublisher, never()).publishCommand(any(KnowledgeChunkRebuildCommandMessage.class));
        verify(valueOperations, never()).increment(any());
    }

    @Test
    void updateDocumentChunkContent_WhenVectorIdInvalid_ShouldThrow() {
        DocumentChunkUpdateContentRequest request = new DocumentChunkUpdateContentRequest();
        request.setId(2001L);
        request.setContent("新的内容");

        KbDocumentChunk chunk = newChunk("旧的切片内容");
        chunk.setVectorId("abc");
        when(kbDocumentChunkMapper.selectById(2001L)).thenReturn(chunk);
        when(kbDocumentMapper.selectById(1001L)).thenReturn(newDocument());
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(newKbBase());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> kbDocumentChunkService.updateDocumentChunkContent(request));

        assertEquals("向量ID无效", ex.getMessage());
        verify(kbDocumentChunkHistoryService, never()).save(any(KbDocumentChunkHistory.class));
        verify(knowledgeChunkRebuildPublisher, never()).publishCommand(any(KnowledgeChunkRebuildCommandMessage.class));
    }

    @Test
    void updateDocumentChunkContent_WhenPublishFails_ShouldKeepContentAndMarkFailed() {
        DocumentChunkUpdateContentRequest request = new DocumentChunkUpdateContentRequest();
        request.setId(2001L);
        request.setContent("新的切片内容");

        when(kbDocumentChunkMapper.selectById(2001L)).thenReturn(newChunk("旧的切片内容"));
        when(kbDocumentMapper.selectById(1001L)).thenReturn(newDocument());
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(newKbBase());
        when(kbDocumentChunkMapper.updateById(any(KbDocumentChunk.class))).thenReturn(1);
        when(kbDocumentChunkHistoryService.save(any(KbDocumentChunkHistory.class))).thenReturn(true);
        when(valueOperations.increment("kb:chunk_edit:latest_version:900001")).thenReturn(4L);
        when(redisTemplate.expire("kb:chunk_edit:latest_version:900001", 7L, TimeUnit.DAYS)).thenReturn(true);
        doThrow(new ServiceException("mq error"))
                .when(knowledgeChunkRebuildPublisher)
                .publishCommand(any(KnowledgeChunkRebuildCommandMessage.class));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> kbDocumentChunkService.updateDocumentChunkContent(request));

        assertEquals("内容已保存，但向量重建未成功提交: mq error", ex.getMessage());
        ArgumentCaptor<KbDocumentChunk> chunkCaptor = ArgumentCaptor.forClass(KbDocumentChunk.class);
        verify(kbDocumentChunkMapper, org.mockito.Mockito.times(2)).updateById(chunkCaptor.capture());
        assertEquals("PENDING", chunkCaptor.getAllValues().get(0).getEditStatus());
        assertEquals("FAILED", chunkCaptor.getAllValues().get(1).getEditStatus());
    }

    @Test
    void handleChunkRebuildResult_WhenStarted_ShouldUpdateEditStatus() {
        KnowledgeChunkRebuildResultMessage message = KnowledgeChunkRebuildResultMessage.builder()
                .task_uuid("task-1")
                .document_id(1001L)
                .vector_id(900001L)
                .version(3L)
                .stage("STARTED")
                .build();
        when(valueOperations.get("kb:chunk_edit:latest_version:900001")).thenReturn(3L);
        when(kbDocumentChunkMapper.selectOne(any())).thenReturn(newChunk("旧的切片内容"));
        when(kbDocumentChunkMapper.updateById(any(KbDocumentChunk.class))).thenReturn(1);

        kbDocumentChunkService.handleChunkRebuildResult(message);

        ArgumentCaptor<KbDocumentChunk> captor = ArgumentCaptor.forClass(KbDocumentChunk.class);
        verify(kbDocumentChunkMapper).updateById(captor.capture());
        assertEquals("STARTED", captor.getValue().getEditStatus());
    }

    @Test
    void handleChunkRebuildResult_WhenCompleted_ShouldUpdateEditStatus() {
        KnowledgeChunkRebuildResultMessage message = KnowledgeChunkRebuildResultMessage.builder()
                .task_uuid("task-2")
                .document_id(1001L)
                .vector_id(900001L)
                .version(3L)
                .stage("COMPLETED")
                .build();
        when(valueOperations.get("kb:chunk_edit:latest_version:900001")).thenReturn(3L);
        when(kbDocumentChunkMapper.selectOne(any())).thenReturn(newChunk("旧的切片内容"));
        when(kbDocumentChunkMapper.updateById(any(KbDocumentChunk.class))).thenReturn(1);

        kbDocumentChunkService.handleChunkRebuildResult(message);

        ArgumentCaptor<KbDocumentChunk> captor = ArgumentCaptor.forClass(KbDocumentChunk.class);
        verify(kbDocumentChunkMapper).updateById(captor.capture());
        assertEquals("COMPLETED", captor.getValue().getEditStatus());
    }

    @Test
    void handleChunkRebuildResult_WhenFailed_ShouldUpdateEditStatus() {
        KnowledgeChunkRebuildResultMessage message = KnowledgeChunkRebuildResultMessage.builder()
                .task_uuid("task-3")
                .document_id(1001L)
                .vector_id(900001L)
                .version(3L)
                .stage("FAILED")
                .message("已被更新版本替代")
                .build();
        when(valueOperations.get("kb:chunk_edit:latest_version:900001")).thenReturn(3L);
        when(kbDocumentChunkMapper.selectOne(any())).thenReturn(newChunk("旧的切片内容"));
        when(kbDocumentChunkMapper.updateById(any(KbDocumentChunk.class))).thenReturn(1);

        kbDocumentChunkService.handleChunkRebuildResult(message);

        ArgumentCaptor<KbDocumentChunk> captor = ArgumentCaptor.forClass(KbDocumentChunk.class);
        verify(kbDocumentChunkMapper).updateById(captor.capture());
        assertEquals("FAILED", captor.getValue().getEditStatus());
    }

    @Test
    void handleChunkRebuildResult_WhenStaleVersion_ShouldDrop() {
        KnowledgeChunkRebuildResultMessage message = KnowledgeChunkRebuildResultMessage.builder()
                .task_uuid("task-4")
                .document_id(1001L)
                .vector_id(900001L)
                .version(2L)
                .stage("COMPLETED")
                .build();
        when(valueOperations.get("kb:chunk_edit:latest_version:900001")).thenReturn(3L);

        kbDocumentChunkService.handleChunkRebuildResult(message);

        verify(kbDocumentChunkMapper, never()).selectOne(any());
        verify(kbDocumentChunkMapper, never()).updateById(any(KbDocumentChunk.class));
    }

    private KbDocumentChunk newChunk(String content) {
        KbDocumentChunk chunk = new KbDocumentChunk();
        chunk.setId(2001L);
        chunk.setDocumentId(1001L);
        chunk.setVectorId("900001");
        chunk.setContent(content);
        return chunk;
    }

    private KbDocument newDocument() {
        KbDocument document = new KbDocument();
        document.setId(1001L);
        document.setKnowledgeBaseId(1L);
        return document;
    }

    private KbBase newKbBase() {
        KbBase kbBase = new KbBase();
        kbBase.setId(1L);
        kbBase.setKnowledgeName("drug_faq");
        kbBase.setEmbeddingModel("text-embedding-v4");
        return kbBase;
    }
}
