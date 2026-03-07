package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.model.enums.KnowledgeChunkTaskStageEnum;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkRebuildResultMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KnowledgeChunkRebuildResultListenerTests {

    @Mock
    private KbDocumentChunkService kbDocumentChunkService;

    @InjectMocks
    private KnowledgeChunkRebuildResultListener listener;

    @Test
    void handle_WhenMessageNull_ShouldSkip() {
        listener.handle(null);
        verify(kbDocumentChunkService, never()).handleChunkRebuildResult(null);
    }

    @Test
    void handle_ShouldDelegateToService() {
        KnowledgeChunkRebuildResultMessage message = KnowledgeChunkRebuildResultMessage.builder()
                .task_uuid("task-1")
                .document_id(1001L)
                .vector_id(900001L)
                .version(3L)
                .stage(KnowledgeChunkTaskStageEnum.COMPLETED.getCode())
                .build();

        listener.handle(message);

        verify(kbDocumentChunkService).handleChunkRebuildResult(message);
    }
}
