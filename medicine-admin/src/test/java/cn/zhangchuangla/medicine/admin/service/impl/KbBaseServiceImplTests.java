package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.KnowledgeBaseAiClient;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KbBaseServiceImplTests {

    @Mock
    private KnowledgeBaseAiClient knowledgeBaseAiClient;

    @Spy
    @InjectMocks
    private KbBaseServiceImpl kbBaseService;

    @Test
    void addKnowledgeBase_ShouldUseBusinessNameAndMilvusNameFromId() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setDescription("覆盖常见用药相关问答内容");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1024);
        request.setStatus(0);

        doReturn(false).when(kbBaseService).isKnowledgeNameExists("drug_faq");
        doReturn("kb_1741096505123").when(kbBaseService).generateMilvusCollectionName();
        doReturn(false).when(kbBaseService).isMilvusCollectionNameExists("kb_1741096505123");
        doReturn("admin").when(kbBaseService).getUsername();
        doReturn(true).when(kbBaseService).save(any(KbBase.class));

        boolean result = kbBaseService.addKnowledgeBase(request);

        assertTrue(result);
        ArgumentCaptor<KbBase> captor = ArgumentCaptor.forClass(KbBase.class);
        verify(kbBaseService).save(captor.capture());
        KbBase saved = captor.getValue();
        assertEquals("drug_faq", saved.getKnowledgeName());
        assertEquals("kb_1741096505123", saved.getMilvusCollectionName());
        assertEquals("text-embedding-3-large", saved.getEmbeddingModel());
        assertEquals(1024, saved.getEmbeddingDim());
        verify(kbBaseService).generateMilvusCollectionName();
        verify(kbBaseService).isMilvusCollectionNameExists("kb_1741096505123");

        verify(knowledgeBaseAiClient).createKnowledgeBase(
                eq("kb_1741096505123"),
                eq(1024),
                eq("覆盖常见用药相关问答内容")
        );
        InOrder inOrder = inOrder(knowledgeBaseAiClient, kbBaseService);
        inOrder.verify(knowledgeBaseAiClient).createKnowledgeBase("kb_1741096505123", 1024, "覆盖常见用药相关问答内容");
        inOrder.verify(kbBaseService).save(any(KbBase.class));
    }

    @Test
    void addKnowledgeBase_WhenKnowledgeNameExists_ShouldThrowException() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1024);

        doReturn(true).when(kbBaseService).isKnowledgeNameExists("drug_faq");

        assertThrows(ServiceException.class, () -> kbBaseService.addKnowledgeBase(request));
        verify(kbBaseService, never()).save(any(KbBase.class));
        verify(knowledgeBaseAiClient, never()).createKnowledgeBase(anyString(), anyInt(), any());
    }

    @Test
    void addKnowledgeBase_WhenAiCallFailed_ShouldThrowException() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1024);

        doReturn(false).when(kbBaseService).isKnowledgeNameExists("drug_faq");
        doReturn("kb_1741096505123").when(kbBaseService).generateMilvusCollectionName();
        doReturn(false).when(kbBaseService).isMilvusCollectionNameExists("kb_1741096505123");
        doThrow(new ServiceException("AI服务异常")).when(knowledgeBaseAiClient)
                .createKnowledgeBase(anyString(), anyInt(), any());

        assertThrows(ServiceException.class, () -> kbBaseService.addKnowledgeBase(request));
        verify(kbBaseService, never()).save(any(KbBase.class));
    }

    @Test
    void addKnowledgeBase_WhenMilvusNameExists_ShouldRetryGenerate() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setDescription("覆盖常见用药相关问答内容");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1024);

        doReturn(false).when(kbBaseService).isKnowledgeNameExists("drug_faq");
        doReturn("kb_1741096505000", "kb_1741096505001").when(kbBaseService).generateMilvusCollectionName();
        doReturn(true, false).when(kbBaseService).isMilvusCollectionNameExists(anyString());
        doReturn("admin").when(kbBaseService).getUsername();
        doReturn(true).when(kbBaseService).save(any(KbBase.class));

        boolean result = kbBaseService.addKnowledgeBase(request);

        assertTrue(result);
        verify(kbBaseService, times(2)).generateMilvusCollectionName();
        verify(knowledgeBaseAiClient).createKnowledgeBase("kb_1741096505001", 1024, "覆盖常见用药相关问答内容");
    }

    @Test
    void updateKnowledgeBase_ShouldOnlyUpdateMutableFields() {
        KbBase existing = new KbBase();
        existing.setId(1L);
        existing.setKnowledgeName("drug_faq");
        existing.setMilvusCollectionName("kb_1");
        existing.setEmbeddingModel("text-embedding-3-large");
        existing.setEmbeddingDim(1024);
        existing.setDisplayName("旧名称");
        existing.setDescription("旧描述");
        existing.setStatus(0);
        existing.setUpdateBy("old_admin");
        existing.setUpdatedAt(new Date(1_700_000_000_000L));

        KnowledgeBaseUpdateRequest request = new KnowledgeBaseUpdateRequest();
        request.setId(1L);
        request.setDisplayName("新名称");
        request.setDescription("新描述");
        request.setStatus(1);

        doReturn(existing).when(kbBaseService).getById(1L);
        doReturn("admin").when(kbBaseService).getUsername();
        doReturn(true).when(kbBaseService).updateById(any(KbBase.class));

        boolean result = kbBaseService.updateKnowledgeBase(request);

        assertTrue(result);
        ArgumentCaptor<KbBase> captor = ArgumentCaptor.forClass(KbBase.class);
        verify(kbBaseService).updateById(captor.capture());
        KbBase updated = captor.getValue();
        assertEquals("drug_faq", updated.getKnowledgeName());
        assertEquals("kb_1", updated.getMilvusCollectionName());
        assertEquals("text-embedding-3-large", updated.getEmbeddingModel());
        assertEquals(1024, updated.getEmbeddingDim());
        assertEquals("新名称", updated.getDisplayName());
        assertEquals("新描述", updated.getDescription());
        assertEquals(1, updated.getStatus());
        assertEquals("admin", updated.getUpdateBy());
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().after(new Date(1_700_000_000_000L)));
    }

}
