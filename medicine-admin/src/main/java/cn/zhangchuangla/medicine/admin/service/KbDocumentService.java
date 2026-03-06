package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.DocumentDeleteRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentListRequest;
import cn.zhangchuangla.medicine.admin.model.request.DocumentSliceUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface KbDocumentService extends IService<KbDocument> {

    /**
     * 分页查询文档列表。
     *
     * @param request 查询参数
     * @return 分页结果
     */
    Page<KbDocument> listDocument(DocumentListRequest request);

    /**
     * 根据ID查询文档详情。
     *
     * @param id 文档ID
     * @return 文档详情
     */
    KbDocument getDocumentById(Long id);

    /**
     * 删除文档及其关联切片。
     *
     * @param id 文档ID
     * @return 删除结果
     */
    boolean deleteDocument(Long id);

    /**
     * 批量删除文档及其关联切片。
     *
     * @param request 删除请求
     * @return 删除结果
     */
    boolean deleteDocuments(DocumentDeleteRequest request);

    /**
     * 更新文档切片状态。
     *
     * @param request 更新请求
     * @return 更新结果
     */
    boolean updateDocumentChunkStatus(DocumentSliceUpdateRequest request);

    /**
     * 发起文档导入任务。
     *
     * @param request 导入请求
     */
    void importDocument(KnowledgeBaseImportRequest request);

    /**
     * 处理知识库导入结果消息。
     *
     * @param message 结果消息
     */
    void handleImportResult(KnowledgeImportResultMessage message);

    /**
     * 处理切片同步消息。
     *
     * @param message 切片同步消息
     */
    void handleChunkUpdateResult(KnowledgeImportResultMessage message);

}
