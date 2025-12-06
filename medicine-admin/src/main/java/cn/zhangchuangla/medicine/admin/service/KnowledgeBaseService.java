package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseDocumentVo;
import cn.zhangchuangla.medicine.admin.model.vo.KnowledgeBaseListVo;
import cn.zhangchuangla.medicine.model.entity.KnowledgeBase;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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
    Page<KnowledgeBaseListVo> listKnowledgeBase(KnowledgeBaseListRequest request);

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

    /**
     * 导入知识库
     *
     * @param request 导入参数
     * @return 是否导入成功
     */
    boolean importKnowledgeBase(KnowledgeBaseImportRequest request);

    /**
     * 删除知识库文档（同步删除数据库，异步删除向量）。
     *
     * @param request 删除参数
     * @return 是否删除成功
     */
    boolean deleteDocument(DocumentDeleteRequest request);

    /**
     * 异步消息消费时调用的导入逻辑（包含切片与向量写入）。
     *
     * @param knowledgeBaseId 知识库ID
     * @param fileUrls        文件地址列表
     * @param username        操作人用户名（在 MQ 发布时传入）
     */
    void ingestKnowledgeBase(Integer knowledgeBaseId, List<String> fileUrls, String username);

    /**
     * 异步删除 Milvus 向量。
     *
     * @param knowledgeBaseId 知识库ID
     * @param vectorIds       向量ID列表
     */
    void deleteDocumentVectors(Integer knowledgeBaseId, List<String> vectorIds);

    /**
     * 文档列表
     *
     * @param id      知识库ID
     * @param request 文档列表参数
     * @return 文档列表
     */
    Page<KnowledgeBaseDocumentVo> documentList(Integer id, DocumentListRequest request);
}
