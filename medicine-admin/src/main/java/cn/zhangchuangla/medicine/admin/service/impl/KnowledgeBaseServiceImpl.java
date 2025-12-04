package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.KnowledgeBaseMapper;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.milvus.service.MilvusKnowledgeBaseService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.KnowledgeBase;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 知识库 Service 实现
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
        implements KnowledgeBaseService {

    private final MilvusKnowledgeBaseService milvusKnowledgeBaseService;

    @Override
    public Page<KnowledgeBase> listKnowledgeBase(KnowledgeBaseListRequest request) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(KnowledgeBase::getName, request.getName());
        }
        if (StringUtils.hasText(request.getDescription())) {
            wrapper.like(KnowledgeBase::getDescription, request.getDescription());
        }
        wrapper.orderByDesc(KnowledgeBase::getCreateTime);
        return page(request.toPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addKnowledgeBase(KnowledgeBaseAddRequest request) {
        if (isNameDuplicated(request.getName(), null)) {
            throw new ServiceException("知识库名称已存在");
        }
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        BeanUtils.copyProperties(request, knowledgeBase);

        String username = SecurityUtils.getUsername();
        Date now = new Date();
        knowledgeBase.setCreateTime(now);
        knowledgeBase.setUpdateTime(now);
        knowledgeBase.setCreateBy(username);
        knowledgeBase.setUpdateBy(username);

        boolean saved = save(knowledgeBase);
        if (!saved) {
            return false;
        }

        // 同步创建 Milvus 集合，以便后续写入知识库的向量数据
        milvusKnowledgeBaseService.createKnowledgeBaseSpace(knowledgeBase.getId());
        return true;
    }

    @Override
    public KnowledgeBase getKnowledgeBase(Integer id) {
        KnowledgeBase knowledgeBase = getById(id);
        if (knowledgeBase == null) {
            throw new ServiceException("知识库不存在");
        }
        return knowledgeBase;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateKnowledgeBase(KnowledgeBaseUpdateRequest request) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(request.getId());
        if (isNameDuplicated(request.getName(), request.getId())) {
            throw new ServiceException("知识库名称已存在");
        }

        BeanUtils.copyProperties(request, knowledgeBase);
        knowledgeBase.setUpdateTime(new Date());
        knowledgeBase.setUpdateBy(SecurityUtils.getUsername());

        return updateById(knowledgeBase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteKnowledgeBase(Integer id) {
        KnowledgeBase knowledgeBase = getKnowledgeBase(id);

        // 删除向量库集合，保证数据库和向量库一致
        milvusKnowledgeBaseService.dropKnowledgeBaseSpace(id);

        return removeById(knowledgeBase.getId());
    }

    private boolean isNameDuplicated(String name, Integer excludeId) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getName, name);
        if (excludeId != null) {
            wrapper.ne(KnowledgeBase::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}

