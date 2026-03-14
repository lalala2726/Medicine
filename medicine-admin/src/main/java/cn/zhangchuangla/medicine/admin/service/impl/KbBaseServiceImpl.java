package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.MedicineAgentClient;
import cn.zhangchuangla.medicine.admin.mapper.KbBaseMapper;
import cn.zhangchuangla.medicine.admin.model.dto.KnowledgeBaseListDto;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.admin.service.AgentConfigRuntimeSyncService;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.admin.service.LlmProviderModelService;
import cn.zhangchuangla.medicine.admin.service.LlmProviderService;
import cn.zhangchuangla.medicine.admin.support.KnowledgeBaseEmbeddingDimSupport;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.constants.LlmModelTypeConstants;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import cn.zhangchuangla.medicine.model.entity.LlmProvider;
import cn.zhangchuangla.medicine.model.entity.LlmProviderModel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class KbBaseServiceImpl extends ServiceImpl<KbBaseMapper, KbBase>
        implements KbBaseService, BaseService {

    private static final int PROVIDER_STATUS_ENABLED = 1;
    /**
     * 启用状态：0。
     */
    private static final int STATUS_ENABLED = 0;

    /**
     * 禁用状态：1。
     */
    private static final int STATUS_DISABLED = 1;
    private static final int MODEL_STATUS_ENABLED = 0;
    private static final String ENABLED_PROVIDER_MISSING_MESSAGE = "当前没有启用的模型提供商";
    private static final String EMBEDDING_MODEL_INVALID_MESSAGE = "向量模型必须是当前激活提供商下的启用向量模型：%s";

    private final MedicineAgentClient medicineAgentClient;
    private final AgentConfigRuntimeSyncService agentConfigRuntimeSyncService;
    private final LlmProviderService llmProviderService;
    private final LlmProviderModelService llmProviderModelService;

    @Override
    public Page<KnowledgeBaseListDto> listKnowledgeBase(KnowledgeBaseListRequest request) {
        Assert.notNull(request, "查询参数不能为空");
        Page<KnowledgeBaseListDto> page = request.toPage();
        return baseMapper.listKnowledgeBase(page, request);
    }

    @Override
    public KbBase getKnowledgeBaseById(Long id) {
        Assert.isPositive(id, "知识库ID必须大于0");
        KbBase kbBase = getById(id);
        Assert.isTrue(kbBase != null, "知识库不存在");
        return kbBase;
    }

    @Override
    public List<KbBase> listEnabledKnowledgeBases() {
        return lambdaQuery()
                .eq(KbBase::getStatus, STATUS_ENABLED)
                .orderByAsc(KbBase::getId)
                .list();
    }

    @Override
    public List<KbBase> listEnabledKnowledgeBasesByNames(List<String> knowledgeNames) {
        if (knowledgeNames == null || knowledgeNames.isEmpty()) {
            return List.of();
        }
        return lambdaQuery()
                .eq(KbBase::getStatus, STATUS_ENABLED)
                .in(KbBase::getKnowledgeName, knowledgeNames)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addKnowledgeBase(KnowledgeBaseAddRequest request) {
        Assert.notNull(request, "知识库信息不能为空");
        Assert.notEmpty(request.getKnowledgeName(), "知识库名称不能为空");
        String embeddingModel = validateEmbeddingModel(request.getEmbeddingModel());
        validateEmbeddingDim(request.getEmbeddingDim());

        if (isKnowledgeNameExists(request.getKnowledgeName())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "知识库名称已存在");
        }

        medicineAgentClient.createKnowledgeBase(request.getKnowledgeName(), request.getEmbeddingDim(), request.getDescription());

        KbBase kbBase = copyProperties(request, KbBase.class);
        kbBase.setCover(normalizeCover(request.getCover()));
        kbBase.setEmbeddingModel(embeddingModel);
        kbBase.setCreateBy(getUsername());
        try {
            boolean saved = save(kbBase);
            if (!saved) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "创建知识库失败");
            }
            return true;
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "知识库名称已存在");
        }
    }

    @Override
    public boolean updateKnowledgeBase(KnowledgeBaseUpdateRequest request) {
        Assert.notNull(request, "知识库信息不能为空");
        Assert.isPositive(request.getId(), "知识库ID必须大于0");
        KbBase existingKbBase = getById(request.getId());
        Assert.isTrue(existingKbBase != null, "知识库不存在");
        applyStatusChangeIfNecessary(existingKbBase, request.getStatus());

        existingKbBase.setDisplayName(request.getDisplayName());
        existingKbBase.setCover(normalizeCover(request.getCover()));
        existingKbBase.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            existingKbBase.setStatus(request.getStatus());
        }
        existingKbBase.setUpdateBy(getUsername());
        existingKbBase.setUpdatedAt(new Date());
        return updateById(existingKbBase);
    }

    @Override
    public boolean deleteKnowledgeBase(Long id) {
        Assert.isPositive(id, "知识库ID必须大于0");
        KbBase kbBase = getKnowledgeBaseById(id);
        agentConfigRuntimeSyncService.assertKnowledgeBaseCanDelete(kbBase.getKnowledgeName());
        return removeById(id);
    }

    /**
     * 判断业务知识库名称是否已被占用。
     *
     * @param knowledgeName 业务知识库名称
     * @return true: 已存在；false: 不存在
     */
    boolean isKnowledgeNameExists(String knowledgeName) {
        return lambdaQuery()
                .eq(KbBase::getKnowledgeName, knowledgeName)
                .count() > 0;
    }

    /**
     * 校验知识库向量维度是否属于支持集合。
     *
     * @param embeddingDim 向量维度
     */
    private void validateEmbeddingDim(Integer embeddingDim) {
        Assert.notNull(embeddingDim, "向量维度不能为空");
        Assert.isParamTrue(KnowledgeBaseEmbeddingDimSupport.isSupported(embeddingDim),
                KnowledgeBaseEmbeddingDimSupport.SUPPORTED_DIM_MESSAGE);
    }

    /**
     * 校验知识库向量模型是否属于当前激活提供商的启用向量模型。
     *
     * @param embeddingModel 向量模型名称
     * @return 归一化后的向量模型名称
     */
    private String validateEmbeddingModel(String embeddingModel) {
        Assert.notEmpty(embeddingModel, "向量模型标识不能为空");
        String normalizedEmbeddingModel = embeddingModel.trim();
        LlmProvider provider = getRequiredEnabledProvider();
        List<LlmProviderModel> models = llmProviderModelService.lambdaQuery()
                .eq(LlmProviderModel::getProviderId, provider.getId())
                .eq(LlmProviderModel::getModelType, LlmModelTypeConstants.EMBEDDING)
                .eq(LlmProviderModel::getModelName, normalizedEmbeddingModel)
                .eq(LlmProviderModel::getEnabled, MODEL_STATUS_ENABLED)
                .orderByAsc(LlmProviderModel::getSort, LlmProviderModel::getId)
                .list();
        if (models.isEmpty()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    EMBEDDING_MODEL_INVALID_MESSAGE.formatted(normalizedEmbeddingModel));
        }
        return normalizedEmbeddingModel;
    }

    /**
     * 查询当前激活提供商，不存在时抛出异常。
     *
     * @return 当前激活提供商
     */
    private LlmProvider getRequiredEnabledProvider() {
        List<LlmProvider> providers = llmProviderService.lambdaQuery()
                .eq(LlmProvider::getStatus, PROVIDER_STATUS_ENABLED)
                .orderByAsc(LlmProvider::getSort, LlmProvider::getId)
                .list();
        if (providers.isEmpty()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, ENABLED_PROVIDER_MISSING_MESSAGE);
        }
        return providers.getFirst();
    }

    private void applyStatusChangeIfNecessary(KbBase kbBase, Integer targetStatus) {
        if (targetStatus == null) {
            return;
        }
        Assert.isParamTrue(STATUS_ENABLED == targetStatus || STATUS_DISABLED == targetStatus, "状态值不合法");
        Integer currentStatus = kbBase.getStatus();
        if (currentStatus != null && currentStatus.equals(targetStatus)) {
            return;
        }
        Assert.notEmpty(kbBase.getKnowledgeName(), "知识库名称不能为空");
        if (STATUS_ENABLED == targetStatus) {
            medicineAgentClient.loadKnowledgeBase(kbBase.getKnowledgeName());
            return;
        }
        agentConfigRuntimeSyncService.assertKnowledgeBaseCanDisable(kbBase.getKnowledgeName());
        medicineAgentClient.releaseKnowledgeBase(kbBase.getKnowledgeName());
    }

    private String normalizeCover(String cover) {
        if (cover == null) {
            return null;
        }
        String normalized = cover.strip();
        return normalized.isEmpty() ? null : normalized;
    }
}
