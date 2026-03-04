package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseAddRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseListRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseUpdateRequest;
import cn.zhangchuangla.medicine.model.entity.KbBase;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface KbBaseService extends IService<KbBase> {

    /**
     * 分页查询知识库
     *
     * @param request 查询参数
     * @return 分页结果
     */
    Page<KbBase> listKnowledgeBase(KnowledgeBaseListRequest request);

    /**
     * 根据ID查询知识库
     *
     * @param id 主键ID
     * @return 知识库信息
     */
    KbBase getKnowledgeBaseById(Long id);

    /**
     * 添加知识库
     *
     * @param request 添加参数（包含业务名称 knowledgeName）
     * @return 添加结果
     */
    boolean addKnowledgeBase(KnowledgeBaseAddRequest request);

    /**
     * 修改知识库
     *
     * @param request 修改参数
     * @return 修改结果
     */
    boolean updateKnowledgeBase(KnowledgeBaseUpdateRequest request);

    /**
     * 启用知识库
     *
     * @param id 知识库ID
     * @return 启用结果
     */
    boolean enableKnowledgeBase(Long id);

    /**
     * 禁用知识库
     *
     * @param id 知识库ID
     * @return 禁用结果
     */
    boolean disableKnowledgeBase(Long id);

    /**
     * 删除知识库
     *
     * @param ids 主键ID列表
     * @return 删除结果
     */
    boolean deleteKnowledgeBase(List<Long> ids);

}
