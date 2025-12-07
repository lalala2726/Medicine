package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.DocumentSliceListRequest;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface KbDocumentChunkService extends IService<KbDocumentChunk> {

    /**
     * 保存知识库文档
     *
     * @param chunkEntities 文档切片
     * @return 保存结果
     */
    boolean saveKbDocuments(List<KbDocumentChunk> chunkEntities);

    /**
     * 文档切片列表。
     *
     * @param knowledgeBaseId 知识库ID
     * @param request         查询参数
     * @return 分页结果
     */
    Page<KbDocumentChunk> documentSliceList(Long documentId, DocumentSliceListRequest request);
}
