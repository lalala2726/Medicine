package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.MedicineAgentClient;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.common.core.exception.ParamException;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KbBaseServiceImplTests {

    @Mock
    private MedicineAgentClient medicineAgentClient;

    @Spy
    @InjectMocks
    private KbBaseServiceImpl kbBaseService;

    @Test
    void addKnowledgeBase_ShouldCallAgentWithKnowledgeNameAndSave() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setDescription("覆盖常见用药相关问答内容");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1024);
        request.setStatus(0);

        doReturn(false).when(kbBaseService).isKnowledgeNameExists("drug_faq");
        doReturn("admin").when(kbBaseService).getUsername();
        doReturn(true).when(kbBaseService).save(any(KbBase.class));

        boolean result = kbBaseService.addKnowledgeBase(request);

        assertTrue(result);
        ArgumentCaptor<KbBase> captor = ArgumentCaptor.forClass(KbBase.class);
        verify(kbBaseService).save(captor.capture());
        KbBase saved = captor.getValue();
        assertEquals("drug_faq", saved.getKnowledgeName());
        assertEquals("常见用药知识库", saved.getDisplayName());
        assertEquals("text-embedding-3-large", saved.getEmbeddingModel());
        assertEquals(1024, saved.getEmbeddingDim());

        verify(medicineAgentClient).createKnowledgeBase(
                eq("drug_faq"),
                eq(1024),
                eq("覆盖常见用药相关问答内容")
        );
        InOrder inOrder = inOrder(medicineAgentClient, kbBaseService);
        inOrder.verify(medicineAgentClient).createKnowledgeBase("drug_faq", 1024, "覆盖常见用药相关问答内容");
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
        verify(medicineAgentClient, never()).createKnowledgeBase(anyString(), anyInt(), any());
    }

    @Test
    void addKnowledgeBase_WhenAiCallFailed_ShouldThrowException() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1024);

        doReturn(false).when(kbBaseService).isKnowledgeNameExists("drug_faq");
        doThrow(new ServiceException("Agent服务异常")).when(medicineAgentClient)
                .createKnowledgeBase(anyString(), anyInt(), any());

        assertThrows(ServiceException.class, () -> kbBaseService.addKnowledgeBase(request));
        verify(kbBaseService, never()).save(any(KbBase.class));
    }

    @Test
    void addKnowledgeBase_WhenDuplicateKey_ShouldThrowDuplicateMessage() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setDescription("覆盖常见用药相关问答内容");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1024);

        doReturn(false).when(kbBaseService).isKnowledgeNameExists("drug_faq");
        doReturn("admin").when(kbBaseService).getUsername();
        doThrow(new DuplicateKeyException("duplicate")).when(kbBaseService).save(any(KbBase.class));

        ServiceException exception = assertThrows(ServiceException.class, () -> kbBaseService.addKnowledgeBase(request));
        assertEquals("知识库名称已存在", exception.getMessage());
        verify(medicineAgentClient).createKnowledgeBase("drug_faq", 1024, "覆盖常见用药相关问答内容");
    }

    @Test
    void addKnowledgeBase_WhenEmbeddingDimTooSmall_ShouldThrowParamException() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(64);

        ParamException exception = assertThrows(ParamException.class, () -> kbBaseService.addKnowledgeBase(request));
        assertEquals("向量维度必须在128到8192之间", exception.getMessage());
        verify(medicineAgentClient, never()).createKnowledgeBase(anyString(), anyInt(), any());
        verify(kbBaseService, never()).save(any(KbBase.class));
    }

    @Test
    void addKnowledgeBase_WhenEmbeddingDimNotPowerOfTwo_ShouldThrowParamException() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1000);

        ParamException exception = assertThrows(ParamException.class, () -> kbBaseService.addKnowledgeBase(request));
        assertEquals("向量维度必须是2的幂", exception.getMessage());
        verify(medicineAgentClient, never()).createKnowledgeBase(anyString(), anyInt(), any());
        verify(kbBaseService, never()).save(any(KbBase.class));
    }

    @Test
    void updateKnowledgeBase_ShouldOnlyUpdateMutableFields() {
        KbBase existing = new KbBase();
        existing.setId(1L);
        existing.setKnowledgeName("drug_faq");
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

        doReturn(existing).when(kbBaseService).getById(1L);
        doReturn("admin").when(kbBaseService).getUsername();
        doReturn(true).when(kbBaseService).updateById(any(KbBase.class));

        boolean result = kbBaseService.updateKnowledgeBase(request);

        assertTrue(result);
        ArgumentCaptor<KbBase> captor = ArgumentCaptor.forClass(KbBase.class);
        verify(kbBaseService).updateById(captor.capture());
        KbBase updated = captor.getValue();
        assertEquals("drug_faq", updated.getKnowledgeName());
        assertEquals("text-embedding-3-large", updated.getEmbeddingModel());
        assertEquals(1024, updated.getEmbeddingDim());
        assertEquals("新名称", updated.getDisplayName());
        assertEquals("新描述", updated.getDescription());
        assertEquals(0, updated.getStatus());
        assertEquals("admin", updated.getUpdateBy());
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().after(new Date(1_700_000_000_000L)));
    }

    @Test
    void enableKnowledgeBase_WhenAlreadyEnabled_ShouldReturnTrueWithoutCallingAi() {
        KbBase existing = new KbBase();
        existing.setId(1L);
        existing.setKnowledgeName("drug_faq");
        existing.setStatus(0);

        doReturn(existing).when(kbBaseService).getById(1L);

        boolean result = kbBaseService.enableKnowledgeBase(1L);

        assertTrue(result);
        verify(medicineAgentClient, never()).loadKnowledgeBase(anyString());
        verify(kbBaseService, never()).updateById(any(KbBase.class));
    }

    @Test
    void enableKnowledgeBase_WhenDisabled_ShouldCallAiAndUpdateStatus() {
        KbBase existing = new KbBase();
        existing.setId(1L);
        existing.setKnowledgeName("drug_faq");
        existing.setStatus(1);

        doReturn(existing).when(kbBaseService).getById(1L);
        doReturn("admin").when(kbBaseService).getUsername();
        doReturn(true).when(kbBaseService).updateById(any(KbBase.class));

        boolean result = kbBaseService.enableKnowledgeBase(1L);

        assertTrue(result);
        verify(medicineAgentClient).loadKnowledgeBase("drug_faq");
        ArgumentCaptor<KbBase> captor = ArgumentCaptor.forClass(KbBase.class);
        verify(kbBaseService).updateById(captor.capture());
        KbBase updated = captor.getValue();
        assertEquals(0, updated.getStatus());
        assertEquals("admin", updated.getUpdateBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void enableKnowledgeBase_WhenAiFailed_ShouldThrowExceptionAndNotUpdate() {
        KbBase existing = new KbBase();
        existing.setId(1L);
        existing.setKnowledgeName("drug_faq");
        existing.setStatus(1);

        doReturn(existing).when(kbBaseService).getById(1L);
        doThrow(new ServiceException("Agent启用失败")).when(medicineAgentClient).loadKnowledgeBase("drug_faq");

        assertThrows(ServiceException.class, () -> kbBaseService.enableKnowledgeBase(1L));
        verify(kbBaseService, never()).updateById(any(KbBase.class));
    }

    @Test
    void disableKnowledgeBase_WhenAlreadyDisabled_ShouldReturnTrueWithoutCallingAi() {
        KbBase existing = new KbBase();
        existing.setId(1L);
        existing.setKnowledgeName("drug_faq");
        existing.setStatus(1);

        doReturn(existing).when(kbBaseService).getById(1L);

        boolean result = kbBaseService.disableKnowledgeBase(1L);

        assertTrue(result);
        verify(medicineAgentClient, never()).releaseKnowledgeBase(anyString());
        verify(kbBaseService, never()).updateById(any(KbBase.class));
    }

    @Test
    void disableKnowledgeBase_WhenEnabled_ShouldCallAiAndUpdateStatus() {
        KbBase existing = new KbBase();
        existing.setId(1L);
        existing.setKnowledgeName("drug_faq");
        existing.setStatus(0);

        doReturn(existing).when(kbBaseService).getById(1L);
        doReturn("admin").when(kbBaseService).getUsername();
        doReturn(true).when(kbBaseService).updateById(any(KbBase.class));

        boolean result = kbBaseService.disableKnowledgeBase(1L);

        assertTrue(result);
        verify(medicineAgentClient).releaseKnowledgeBase("drug_faq");
        ArgumentCaptor<KbBase> captor = ArgumentCaptor.forClass(KbBase.class);
        verify(kbBaseService).updateById(captor.capture());
        KbBase updated = captor.getValue();
        assertEquals(1, updated.getStatus());
        assertEquals("admin", updated.getUpdateBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void disableKnowledgeBase_WhenAiFailed_ShouldThrowExceptionAndNotUpdate() {
        KbBase existing = new KbBase();
        existing.setId(1L);
        existing.setKnowledgeName("drug_faq");
        existing.setStatus(0);

        doReturn(existing).when(kbBaseService).getById(1L);
        doThrow(new ServiceException("Agent禁用失败")).when(medicineAgentClient).releaseKnowledgeBase("drug_faq");

        assertThrows(ServiceException.class, () -> kbBaseService.disableKnowledgeBase(1L));
        verify(kbBaseService, never()).updateById(any(KbBase.class));
    }

}
