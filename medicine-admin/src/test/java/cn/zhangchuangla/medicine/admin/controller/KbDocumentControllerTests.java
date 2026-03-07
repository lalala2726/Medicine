package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.DocumentDeleteRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseDocumentVo;
import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.enums.KnowledgeChunkModeEnum;
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
class KbDocumentControllerTests {

    @Mock
    private KbDocumentService kbDocumentService;

    @InjectMocks
    private KbDocumentController kbDocumentController;

    @Test
    void listDocument_ShouldReturnPagedResult() {
        DocumentListRequest request = new DocumentListRequest();
        Page<KbDocument> page = new Page<>(1, 10, 1);
        KbDocument document = new KbDocument();
        document.setId(1001L);
        document.setKnowledgeBaseId(1L);
        document.setFileName("guide.pdf");
        document.setFileType("pdf");
        document.setChunkMode(KnowledgeChunkModeEnum.BALANCED_MODE.getCode());
        document.setChunkSize(1000);
        document.setChunkOverlap(200);
        page.setRecords(List.of(document));
        when(kbDocumentService.listDocument(1L, request)).thenReturn(page);

        var result = kbDocumentController.listDocument(1L, request);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getRows().size());
        KnowledgeBaseDocumentVo row = (KnowledgeBaseDocumentVo) result.getData().getRows().get(0);
        assertEquals("pdf", row.getFileType());
        assertEquals(KnowledgeChunkModeEnum.BALANCED_MODE.getCode(), row.getChunkMode());
        assertEquals(1000, row.getChunkSize());
        assertEquals(200, row.getChunkOverlap());
        verify(kbDocumentService).listDocument(1L, request);
    }

    @Test
    void getDocumentById_ShouldReturnDetail() {
        KbDocument document = new KbDocument();
        document.setId(1001L);
        document.setKnowledgeBaseId(1L);
        document.setFileName("guide.pdf");
        document.setFileType("pdf");
        document.setChunkMode(KnowledgeChunkModeEnum.CUSTOM.getCode());
        document.setChunkSize(888);
        document.setChunkOverlap(88);
        when(kbDocumentService.getDocumentById(1001L)).thenReturn(document);

        var result = kbDocumentController.getDocumentById(1001L);

        assertEquals(200, result.getCode());
        assertEquals("guide.pdf", result.getData().getFileName());
        assertEquals("pdf", result.getData().getFileType());
        assertEquals(KnowledgeChunkModeEnum.CUSTOM.getCode(), result.getData().getChunkMode());
        assertEquals(888, result.getData().getChunkSize());
        assertEquals(88, result.getData().getChunkOverlap());
        verify(kbDocumentService).getDocumentById(1001L);
    }

    @Test
    void deleteDocument_ShouldDelegateToService() {
        DocumentDeleteRequest request = new DocumentDeleteRequest();
        request.setDocumentIds(List.of(1001L, 1002L));
        when(kbDocumentService.deleteDocuments(request)).thenReturn(true);

        var result = kbDocumentController.deleteDocument(request);

        assertEquals(200, result.getCode());
        verify(kbDocumentService).deleteDocuments(request);
    }

    @Test
    void importDocument_ShouldDelegateToService() {
        KnowledgeBaseImportRequest request = new KnowledgeBaseImportRequest();
        request.setKnowledgeBaseId(1L);
        request.setFileDetails(List.of(KnowledgeBaseImportRequest.FileDetail.builder()
                .fileName("file.pdf")
                .fileUrl("https://example.com/file.pdf")
                .fileType("pdf")
                .build()));
        request.setChunkMode(KnowledgeChunkModeEnum.CUSTOM.getCode());
        request.setCustomChunkMode(KnowledgeBaseImportRequest.CustomChunkMode.builder()
                .chunkSize(500)
                .chunkOverlap(100)
                .build());

        var result = kbDocumentController.importDocument(request);

        assertEquals(200, result.getCode());
        verify(kbDocumentService).importDocument(request);
    }
}
