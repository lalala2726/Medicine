package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.DocumentListRequest;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface KbDocumentService extends IService<KbDocument> {

    /**
     * 文档列表
     *
     * @param id      知识库id
     * @param request 请求参数
     * @return 文档列表
     */
    Page<KbDocument> documentPage(Integer id, DocumentListRequest request);

    /**
     * 文档详情
     *
     * @param documentId 文档id
     * @return 文档详情
     */
    KbDocument getDocumentById(Long documentId);
}
