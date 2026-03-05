package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KnowledgeChunkUpdateListenerTests {

    @Mock
    private KbDocumentService kbDocumentService;

    @InjectMocks
    private KnowledgeChunkUpdateListener listener;

    @Test
    void handle_WhenMessageNull_ShouldSkip() {
        listener.handle(null);
        verify(kbDocumentService, never()).handleChunkUpdateResult(null);
    }

    @Test
    void handle_ShouldDelegateToService() {
        KnowledgeImportResultMessage message = KnowledgeImportResultMessage.builder()
                .task_uuid("task-1")
                .biz_key("drug_faq:1001")
                .version(1L)
                .stage("COMPLETED")
                .build();

        listener.handle(message);

        verify(kbDocumentService).handleChunkUpdateResult(message);
    }
}
