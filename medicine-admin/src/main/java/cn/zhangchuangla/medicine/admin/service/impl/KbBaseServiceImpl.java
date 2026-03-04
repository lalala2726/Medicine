package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.integration.KnowledgeBaseAiClient;
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
import org.springframework.dao.DuplicateKeyException;
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
     * Milvus 集合名称固定前缀。
     */
    private static final String KB_PREFIX = "kb_";

    /**
     * 创建重试上限（处理极端并发下的唯一键冲突）。
     */
    private static final int MAX_CREATE_RETRY = 5;

    private final KnowledgeBaseAiClient knowledgeBaseAiClient;

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
        Assert.isPositive(request.getEmbeddingDim(), "向量维度必须大于0");

        if (isKnowledgeNameExists(request.getKnowledgeName())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "知识库名称已存在");
        }

        for (int i = 0; i < MAX_CREATE_RETRY; i++) {
            String milvusCollectionName = generateMilvusCollectionName();
            if (isMilvusCollectionNameExists(milvusCollectionName)) {
                continue;
            }

            // 约束：必须先确保 AI 服务端创建成功，再插入本地库。
            knowledgeBaseAiClient.createKnowledgeBase(milvusCollectionName, request.getEmbeddingDim(), request.getDescription());

            KbBase kbBase = copyProperties(request, KbBase.class);
            kbBase.setMilvusCollectionName(milvusCollectionName);
            kbBase.setCreateBy(getUsername());
            try {
                boolean saved = save(kbBase);
                if (!saved) {
                    throw new ServiceException(ResponseCode.OPERATION_ERROR, "创建知识库失败");
                }
                return true;
            } catch (DuplicateKeyException ex) {
                if (isKnowledgeNameExists(request.getKnowledgeName())) {
                    throw new ServiceException(ResponseCode.OPERATION_ERROR, "知识库名称已存在");
                }
                if (i == MAX_CREATE_RETRY - 1) {
                    throw new ServiceException(ResponseCode.OPERATION_ERROR, "创建知识库失败，请稍后重试");
                }
            }
        }

        throw new ServiceException(ResponseCode.OPERATION_ERROR, "创建知识库失败，请稍后重试");
    }

    @Override
    public boolean updateKnowledgeBase(KnowledgeBaseUpdateRequest request) {
        Assert.notNull(request, "知识库信息不能为空");
        Assert.isPositive(request.getId(), "知识库ID必须大于0");
        KbBase existingKbBase = getById(request.getId());
        Assert.isTrue(existingKbBase != null, "知识库不存在");

        existingKbBase.setDisplayName(request.getDisplayName());
        existingKbBase.setDescription(request.getDescription());
        existingKbBase.setStatus(request.getStatus());
        existingKbBase.setUpdateBy(getUsername());
        existingKbBase.setUpdatedAt(new Date());
        return updateById(existingKbBase);
    }

    @Override
    public boolean deleteKnowledgeBase(List<Long> ids) {
        Assert.notEmpty(ids, "知识库ID不能为空");
        return removeByIds(ids);
    }

    /**
     * 判断业务知识库名称在未删除记录中是否已被占用。
     *
     * @param knowledgeName 业务知识库名称
     * @return true: 已存在；false: 不存在
     */
    boolean isKnowledgeNameExists(String knowledgeName) {
        return lambdaQuery()
                .eq(KbBase::getKnowledgeName, knowledgeName)
                .eq(KbBase::getIsDeleted, 0)
                .count() > 0;
    }

    /**
     * 判断 Milvus 集合名称是否已存在。
     *
     * @param milvusCollectionName Milvus 集合名称
     * @return true: 已存在；false: 不存在
     */
    boolean isMilvusCollectionNameExists(String milvusCollectionName) {
        return lambdaQuery()
                .eq(KbBase::getMilvusCollectionName, milvusCollectionName)
                .count() > 0;
    }

    /**
     * 生成 Milvus 集合名称。
     * <p>
     * 规则：kb_ + 当前毫秒时间戳，不限制总长度。
     * </p>
     *
     * @return 形如 kb_1741096505123 的 Milvus 集合名称
     */
    String generateMilvusCollectionName() {
        return KB_PREFIX + System.currentTimeMillis();
    }

}
