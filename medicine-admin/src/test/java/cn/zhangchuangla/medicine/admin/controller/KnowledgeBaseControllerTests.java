package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseControllerTests {

    @Mock
    private KbBaseService kbBaseService;

    @Mock
    private KbDocumentService kbDocumentService;

    @InjectMocks
    private KnowledgeBaseController knowledgeBaseController;

    @Test
    void listKnowledgeBase_ShouldReturnPagedResult() {
        KnowledgeBaseListRequest request = new KnowledgeBaseListRequest();
        Page<KbBase> page = new Page<>(1, 10, 1);
        KbBase kbBase = new KbBase();
        kbBase.setId(1L);
        kbBase.setKnowledgeName("kb_abc1234");
        page.setRecords(List.of(kbBase));
        when(kbBaseService.listKnowledgeBase(request)).thenReturn(page);

        var result = knowledgeBaseController.listKnowledgeBase(request);

        assertEquals(200, result.getCode());
        verify(kbBaseService).listKnowledgeBase(request);
    }

    @Test
    void getKnowledgeBaseById_ShouldReturnDetail() {
        KbBase kbBase = new KbBase();
        kbBase.setId(1L);
        kbBase.setKnowledgeName("kb_abc1234");
        when(kbBaseService.getKnowledgeBaseById(1L)).thenReturn(kbBase);

        var result = knowledgeBaseController.getKnowledgeBaseById(1L);

        assertEquals(200, result.getCode());
        assertEquals("kb_abc1234", result.getData().getKnowledgeName());
        verify(kbBaseService).getKnowledgeBaseById(1L);
    }

    @Test
    void addKnowledgeBase_ShouldDelegateToService() {
        KnowledgeBaseAddRequest request = new KnowledgeBaseAddRequest();
        request.setKnowledgeName("drug_faq");
        request.setDisplayName("常见用药知识库");
        request.setEmbeddingModel("text-embedding-3-large");
        request.setEmbeddingDim(1024);
        when(kbBaseService.addKnowledgeBase(request)).thenReturn(true);

        var result = knowledgeBaseController.addKnowledgeBase(request);

        assertEquals(200, result.getCode());
        verify(kbBaseService).addKnowledgeBase(request);
    }

    @Test
    void updateKnowledgeBase_ShouldDelegateToService() {
        KnowledgeBaseUpdateRequest request = new KnowledgeBaseUpdateRequest();
        request.setId(1L);
        request.setDisplayName("更新后的知识库");
        when(kbBaseService.updateKnowledgeBase(request)).thenReturn(true);

        var result = knowledgeBaseController.updateKnowledgeBase(request);

        assertEquals(200, result.getCode());
        verify(kbBaseService).updateKnowledgeBase(request);
    }

    @Test
    void enableKnowledgeBase_ShouldDelegateToService() {
        when(kbBaseService.enableKnowledgeBase(1L)).thenReturn(true);

        var result = knowledgeBaseController.enableKnowledgeBase(1L);

        assertEquals(200, result.getCode());
        verify(kbBaseService).enableKnowledgeBase(1L);
    }

    @Test
    void disableKnowledgeBase_ShouldDelegateToService() {
        when(kbBaseService.disableKnowledgeBase(1L)).thenReturn(true);

        var result = knowledgeBaseController.disableKnowledgeBase(1L);

        assertEquals(200, result.getCode());
        verify(kbBaseService).disableKnowledgeBase(1L);
    }

    @Test
    void deleteKnowledgeBase_ShouldDelegateToService() {
        when(kbBaseService.deleteKnowledgeBase(List.of(1L, 2L))).thenReturn(true);

        var result = knowledgeBaseController.deleteKnowledgeBase(List.of(1L, 2L));

        assertEquals(200, result.getCode());
        verify(kbBaseService).deleteKnowledgeBase(List.of(1L, 2L));
    }

    @Test
    void importKnowledge_ShouldDelegateToDocumentService() {
        KnowledgeBaseImportRequest request = new KnowledgeBaseImportRequest();
        request.setKnowledgeName("drug_faq");
        request.setFileUrls(List.of("https://example.com/file.pdf"));
        request.setChunkStrategy("character");
        request.setChunkSize(500);
        request.setTokenSize(100);

        var result = knowledgeBaseController.importKnowledge(request);

        assertEquals(200, result.getCode());
        verify(kbDocumentService).importKnowledge(request);
    }
}
