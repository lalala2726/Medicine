package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkListRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkUpdateContentRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentChunkUpdateStatusRequest;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KbDocumentChunkControllerTests {

    @Mock
    private KbDocumentChunkService kbDocumentChunkService;

    @InjectMocks
    private KbDocumentChunkController kbDocumentChunkController;

    @Test
    void listDocumentChunk_ShouldReturnPagedResult() {
        DocumentChunkListRequest request = new DocumentChunkListRequest();
        Page<KbDocumentChunk> page = new Page<>(1, 10, 1);
        KbDocumentChunk chunk = new KbDocumentChunk();
        chunk.setId(2001L);
        chunk.setDocumentId(1001L);
        chunk.setContent("切片内容");
        page.setRecords(List.of(chunk));
        when(kbDocumentChunkService.listDocumentChunk(1001L, request)).thenReturn(page);

        var result = kbDocumentChunkController.listDocumentChunk(1001L, request);

        assertEquals(200, result.getCode());
        verify(kbDocumentChunkService).listDocumentChunk(1001L, request);
    }

    @Test
    void updateDocumentChunkContent_ShouldDelegateToService() {
        DocumentChunkUpdateContentRequest request = new DocumentChunkUpdateContentRequest();
        request.setId(2001L);
        request.setContent("新的切片内容");
        when(kbDocumentChunkService.updateDocumentChunkContent(request)).thenReturn(true);

        var result = kbDocumentChunkController.updateDocumentChunkContent(request);

        assertEquals(200, result.getCode());
        verify(kbDocumentChunkService).updateDocumentChunkContent(request);
    }

    @Test
    void addDocumentChunk_ShouldDelegateToService() {
        DocumentChunkAddRequest request = new DocumentChunkAddRequest();
        request.setDocumentId(1001L);
        request.setContent("新增切片内容");
        when(kbDocumentChunkService.addDocumentChunk(request)).thenReturn(true);

        var result = kbDocumentChunkController.addDocumentChunk(request);

        assertEquals(200, result.getCode());
        verify(kbDocumentChunkService).addDocumentChunk(request);
    }

    @Test
    void updateDocumentChunkStatus_WhenServiceThrows_ShouldPropagate() {
        DocumentChunkUpdateStatusRequest request = new DocumentChunkUpdateStatusRequest();
        request.setId(2001L);
        request.setStatus(1);
        when(kbDocumentChunkService.updateDocumentChunkStatus(request))
                .thenThrow(new ServiceException("切片状态变更暂未开放"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> kbDocumentChunkController.updateDocumentChunkStatus(request));

        assertEquals("切片状态变更暂未开放", ex.getMessage());
        verify(kbDocumentChunkService).updateDocumentChunkStatus(request);
    }

    @Test
    void deleteDocumentChunk_WhenServiceThrows_ShouldPropagate() {
        when(kbDocumentChunkService.deleteDocumentChunk(2001L))
                .thenThrow(new ServiceException("切片删除暂未开放"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> kbDocumentChunkController.deleteDocumentChunk(2001L));

        assertEquals("切片删除暂未开放", ex.getMessage());
        verify(kbDocumentChunkService).deleteDocumentChunk(2001L);
    }
}
