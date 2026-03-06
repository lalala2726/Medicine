package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkAddResultMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KnowledgeChunkAddResultListenerTests {

    @Mock
    private KbDocumentChunkService kbDocumentChunkService;

    @InjectMocks
    private KnowledgeChunkAddResultListener listener;

    @Test
    void handle_WhenMessageNull_ShouldSkip() {
        listener.handle(null);
        verify(kbDocumentChunkService, never()).handleChunkAddResult(null);
    }

    @Test
    void handle_ShouldDelegateToService() {
        KnowledgeChunkAddResultMessage message = KnowledgeChunkAddResultMessage.builder()
                .task_uuid("task-1")
                .chunk_id(3001L)
                .document_id(1001L)
                .stage("COMPLETED")
                .vector_id(900001L)
                .chunk_index(9)
                .build();

        listener.handle(message);

        verify(kbDocumentChunkService).handleChunkAddResult(message);
    }
}
