package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.MedicineAgentClient;
import cn.zhangchuangla.medicine.admin.mapper.KbBaseMapper;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.admin.service.KbBaseService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.KbBase;
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

    /**
     * 启用状态：0。
     */
    private static final int STATUS_ENABLED = 0;

    /**
     * 禁用状态：1。
     */
    private static final int STATUS_DISABLED = 1;

    /**
     * 向量维度最小值。
     */
    private static final int MIN_EMBEDDING_DIM = 128;

    /**
     * 向量维度最大值（2^13）。
     */
    private static final int MAX_EMBEDDING_DIM = 1 << 13;

    private final MedicineAgentClient medicineAgentClient;

    @Override
    public Page<KbBase> listKnowledgeBase(KnowledgeBaseListRequest request) {
        Assert.notNull(request, "查询参数不能为空");
        Page<KbBase> page = request.toPage();
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
    @Transactional(rollbackFor = Exception.class)
    public boolean addKnowledgeBase(KnowledgeBaseAddRequest request) {
        Assert.notNull(request, "知识库信息不能为空");
        Assert.notEmpty(request.getKnowledgeName(), "知识库名称不能为空");
        validateEmbeddingDim(request.getEmbeddingDim());

        if (isKnowledgeNameExists(request.getKnowledgeName())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "知识库名称已存在");
        }

        medicineAgentClient.createKnowledgeBase(request.getKnowledgeName(), request.getEmbeddingDim(), request.getDescription());

        KbBase kbBase = copyProperties(request, KbBase.class);
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

        existingKbBase.setDisplayName(request.getDisplayName());
        existingKbBase.setDescription(request.getDescription());
        existingKbBase.setUpdateBy(getUsername());
        existingKbBase.setUpdatedAt(new Date());
        return updateById(existingKbBase);
    }

    @Override
    public boolean enableKnowledgeBase(Long id) {
        Assert.isPositive(id, "知识库ID必须大于0");
        KbBase kbBase = getById(id);
        Assert.isTrue(kbBase != null, "知识库不存在");

        if (Integer.valueOf(STATUS_ENABLED).equals(kbBase.getStatus())) {
            return true;
        }
        Assert.notEmpty(kbBase.getKnowledgeName(), "知识库名称不能为空");

        medicineAgentClient.loadKnowledgeBase(kbBase.getKnowledgeName());

        kbBase.setStatus(STATUS_ENABLED);
        kbBase.setUpdateBy(getUsername());
        kbBase.setUpdatedAt(new Date());
        return updateById(kbBase);
    }

    @Override
    public boolean disableKnowledgeBase(Long id) {
        Assert.isPositive(id, "知识库ID必须大于0");
        KbBase kbBase = getById(id);
        Assert.isTrue(kbBase != null, "知识库不存在");

        if (Integer.valueOf(STATUS_DISABLED).equals(kbBase.getStatus())) {
            return true;
        }
        Assert.notEmpty(kbBase.getKnowledgeName(), "知识库名称不能为空");

        medicineAgentClient.releaseKnowledgeBase(kbBase.getKnowledgeName());

        kbBase.setStatus(STATUS_DISABLED);
        kbBase.setUpdateBy(getUsername());
        kbBase.setUpdatedAt(new Date());
        return updateById(kbBase);
    }

    @Override
    public boolean deleteKnowledgeBase(List<Long> ids) {
        Assert.notEmpty(ids, "知识库ID不能为空");
        return removeByIds(ids);
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
     * 校验向量维度：范围 [128, 8192] 且必须为 2 的幂。
     *
     * @param embeddingDim 向量维度
     */
    private void validateEmbeddingDim(Integer embeddingDim) {
        Assert.notNull(embeddingDim, "向量维度不能为空");
        Assert.isParamTrue(embeddingDim >= MIN_EMBEDDING_DIM && embeddingDim <= MAX_EMBEDDING_DIM,
                "向量维度必须在128到8192之间");
        Assert.isParamTrue((embeddingDim & (embeddingDim - 1)) == 0, "向量维度必须是2的幂");
    }
}
