package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.model.entity.KnowledgeBase;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 分页查询知识库
     *
     * @param request 查询参数
     * @return 知识库分页数据
     */
    Page<KnowledgeBase> listKnowledgeBase(KnowledgeBaseListRequest request);

    /**
     * 新增知识库
     *
     * @param request 新增参数
     * @return 是否新增成功
     */
    boolean addKnowledgeBase(KnowledgeBaseAddRequest request);

    /**
     * 更新知识库
     *
     * @param request 更新参数
     * @return 是否更新成功
     */
    boolean updateKnowledgeBase(KnowledgeBaseUpdateRequest request);

    /**
     * 删除知识库
     *
     * @param id 知识库ID
     * @return 是否删除成功
     */
    boolean deleteKnowledgeBase(Integer id);

    /**
     * 获取知识库详情
     *
     * @param id 知识库ID
     * @return 知识库详情
     */
    KnowledgeBase getKnowledgeBase(Integer id);
}
