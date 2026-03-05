package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.KnowledgeBaseImportRequest;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface KbDocumentService extends IService<KbDocument> {

    /**
     * 发起知识库文档导入任务。
     *
     * @param request 导入请求
     */
    void importKnowledge(KnowledgeBaseImportRequest request);

    /**
     * 处理知识库导入结果消息。
     *
     * @param message 结果消息
     */
    void handleImportResult(KnowledgeImportResultMessage message);

}
