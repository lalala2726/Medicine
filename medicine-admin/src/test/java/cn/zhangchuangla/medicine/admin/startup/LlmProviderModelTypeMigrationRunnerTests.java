package cn.zhangchuangla.medicine.admin.startup;

import cn.zhangchuangla.medicine.admin.mapper.LlmProviderMapper;
import cn.zhangchuangla.medicine.admin.mapper.LlmProviderModelMapper;
import cn.zhangchuangla.medicine.admin.service.AgentConfigRuntimeSyncService;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmProviderModelTypeMigrationRunnerTests {

    @Mock
    private LlmProviderMapper llmProviderMapper;

    @Mock
    private LlmProviderModelMapper llmProviderModelMapper;

    @Mock
    private AgentConfigRuntimeSyncService agentConfigRuntimeSyncService;

    @InjectMocks
    private LlmProviderModelTypeMigrationRunner runner;

    @Test
    void run_ShouldUpgradeLegacyRerankModelToChatAndRefreshEnabledProvider() throws Exception {
        when(llmProviderModelMapper.selectList(any()))
                .thenReturn(List.of(buildModel(11L, 1L, "legacy-rerank", "RERANK")))
                .thenReturn(List.of());
        when(llmProviderModelMapper.updateById(any(LlmProviderModel.class))).thenReturn(1);
        when(llmProviderMapper.selectList(any())).thenReturn(List.of(buildEnabledProvider(1L)));

        runner.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<LlmProviderModel> updateCaptor = ArgumentCaptor.forClass(LlmProviderModel.class);
        verify(llmProviderModelMapper).updateById(updateCaptor.capture());
        assertEquals(11L, updateCaptor.getValue().getId());
        assertEquals(LlmModelTypeConstants.CHAT, updateCaptor.getValue().getModelType());
        verify(agentConfigRuntimeSyncService).syncActiveProviderSnapshot(buildEnabledProvider(1L), "system-migrate");
        verify(llmProviderModelMapper, never()).deleteById(anyLong());
    }

    @Test
    void run_ShouldDeleteLegacyRerankModelWhenSameNameChatAlreadyExists() throws Exception {
        when(llmProviderModelMapper.selectList(any()))
                .thenReturn(List.of(buildModel(12L, 1L, "shared-model", "RERANK")))
                .thenReturn(List.of(buildModel(13L, 1L, "shared-model", LlmModelTypeConstants.CHAT)));
        when(llmProviderModelMapper.deleteById(12L)).thenReturn(1);
        when(llmProviderMapper.selectList(any())).thenReturn(List.of(buildEnabledProvider(1L)));

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(llmProviderModelMapper).deleteById(12L);
        verify(llmProviderModelMapper, never()).updateById(any(LlmProviderModel.class));
        verify(agentConfigRuntimeSyncService).syncActiveProviderSnapshot(buildEnabledProvider(1L), "system-migrate");
    }

    private LlmProvider buildEnabledProvider(Long id) {
        return LlmProvider.builder()
                .id(id)
                .providerType("openai")
                .baseUrl("https://api.openai.com/v1")
                .apiKey("sk-openai")
                .status(1)
                .build();
    }

    private LlmProviderModel buildModel(Long id, Long providerId, String modelName, String modelType) {
        return LlmProviderModel.builder()
                .id(id)
                .providerId(providerId)
                .modelName(modelName)
                .modelType(modelType)
                .enabled(0)
                .build();
    }
}
