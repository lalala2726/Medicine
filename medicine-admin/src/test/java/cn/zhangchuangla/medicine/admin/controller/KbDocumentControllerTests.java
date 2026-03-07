package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.dto.KnowledgeBaseDocumentDto;
import cn.zhangchuangla.medicine.admin.model.request.DocumentDeleteRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentListRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentUpdateFileNameRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseDocumentVo;
import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
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
        Page<KnowledgeBaseDocumentDto> page = new Page<>(1, 10, 1);
        KnowledgeBaseDocumentDto document = new KnowledgeBaseDocumentDto();
        document.setId(1001L);
        document.setKnowledgeBaseId(1L);
        document.setFileName("guide.pdf");
        document.setFileType("pdf");
        document.setFileSize(1024L);
        document.setChunkCount(12L);
        page.setRecords(List.of(document));
        when(kbDocumentService.listDocument(1L, request)).thenReturn(page);

        var result = kbDocumentController.listDocument(1L, request);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getRows().size());
        KnowledgeBaseDocumentVo row = (KnowledgeBaseDocumentVo) result.getData().getRows().get(0);
        assertEquals("pdf", row.getFileType());
        assertEquals(1024L, row.getFileSize());
        assertEquals(12L, row.getChunkCount());
        verify(kbDocumentService).listDocument(1L, request);
    }

    @Test
    void getDocumentById_ShouldReturnDetail() {
        KbDocument document = new KbDocument();
        document.setId(1001L);
        document.setKnowledgeBaseId(1L);
        document.setFileName("guide.pdf");
        document.setFileType("pdf");
        document.setFileSize(2048L);
        when(kbDocumentService.getDocumentById(1001L)).thenReturn(document);

        var result = kbDocumentController.getDocumentById(1001L);

        assertEquals(200, result.getCode());
        assertEquals("guide.pdf", result.getData().getFileName());
        assertEquals("pdf", result.getData().getFileType());
        assertEquals(2048L, result.getData().getFileSize());
        verify(kbDocumentService).getDocumentById(1001L);
    }

    @Test
    void updateDocumentFileName_ShouldDelegateToService() {
        DocumentUpdateFileNameRequest request = new DocumentUpdateFileNameRequest();
        request.setId(1001L);
        request.setFileName("新的文档名称.pdf");
        when(kbDocumentService.updateDocumentFileName(request)).thenReturn(true);

        var result = kbDocumentController.updateDocumentFileName(request);

        assertEquals(200, result.getCode());
        verify(kbDocumentService).updateDocumentFileName(request);
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
        request.setChunkMode("custom");
        request.setCustomChunkMode(KnowledgeBaseImportRequest.CustomChunkMode.builder()
                .chunkSize(500)
                .chunkOverlap(100)
                .build());

        var result = kbDocumentController.importDocument(request);

        assertEquals(200, result.getCode());
        verify(kbDocumentService).importDocument(request);
    }
}
