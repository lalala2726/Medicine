package cn.zhangchuangla.medicine.admin.startup;

import cn.zhangchuangla.medicine.admin.mapper.LlmProviderMapper;
import cn.zhangchuangla.medicine.admin.mapper.LlmProviderModelMapper;
import cn.zhangchuangla.medicine.admin.service.AgentConfigRuntimeSyncService;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 启动时将历史 RERANK 模型迁移为 CHAT 模型。
 */
@Component
@RequiredArgsConstructor
public class LlmProviderModelTypeMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LlmProviderModelTypeMigrationRunner.class);
    private static final String LEGACY_RERANK_MODEL_TYPE = "RERANK";
    private static final int PROVIDER_STATUS_ENABLED = 1;
    private static final String MIGRATION_OPERATOR = "system-migrate";

    private final LlmProviderMapper llmProviderMapper;
    private final LlmProviderModelMapper llmProviderModelMapper;
    private final AgentConfigRuntimeSyncService agentConfigRuntimeSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        List<LlmProviderModel> legacyModels = llmProviderModelMapper.selectList(Wrappers.<LlmProviderModel>lambdaQuery()
                .eq(LlmProviderModel::getModelType, LEGACY_RERANK_MODEL_TYPE)
                .orderByAsc(LlmProviderModel::getProviderId, LlmProviderModel::getModelName, LlmProviderModel::getId));
        if (legacyModels.isEmpty()) {
            return;
        }

        Set<Long> affectedProviderIds = new LinkedHashSet<>();
        int deletedCount = 0;
        int updatedCount = 0;
        for (LlmProviderModel legacyModel : legacyModels) {
            if (hasSameNameChatModel(legacyModel)) {
                deletedCount += llmProviderModelMapper.deleteById(legacyModel.getId());
            } else {
                updatedCount += llmProviderModelMapper.updateById(LlmProviderModel.builder()
                        .id(legacyModel.getId())
                        .modelType(LlmModelTypeConstants.CHAT)
                        .updateBy(MIGRATION_OPERATOR)
                        .build());
            }
            affectedProviderIds.add(legacyModel.getProviderId());
        }

        refreshAffectedEnabledProviders(affectedProviderIds);
        log.info("Migrated legacy rerank models to chat models, updated={}, deleted={}, affectedProviders={}",
                updatedCount, deletedCount, affectedProviderIds.size());
    }

    private boolean hasSameNameChatModel(LlmProviderModel legacyModel) {
        List<LlmProviderModel> chatModels = llmProviderModelMapper.selectList(Wrappers.<LlmProviderModel>lambdaQuery()
                .eq(LlmProviderModel::getProviderId, legacyModel.getProviderId())
                .eq(LlmProviderModel::getModelName, legacyModel.getModelName())
                .eq(LlmProviderModel::getModelType, LlmModelTypeConstants.CHAT)
                .ne(LlmProviderModel::getId, legacyModel.getId())
                .last("limit 1"));
        return !chatModels.isEmpty();
    }

    private void refreshAffectedEnabledProviders(Set<Long> affectedProviderIds) {
        if (affectedProviderIds.isEmpty()) {
            return;
        }
        List<LlmProvider> enabledProviders = llmProviderMapper.selectList(Wrappers.<LlmProvider>lambdaQuery()
                .in(LlmProvider::getId, affectedProviderIds)
                .eq(LlmProvider::getStatus, PROVIDER_STATUS_ENABLED)
                .orderByAsc(LlmProvider::getSort, LlmProvider::getId));
        enabledProviders.forEach(provider ->
                agentConfigRuntimeSyncService.syncActiveProviderSnapshot(provider, MIGRATION_OPERATOR));
    }
}
