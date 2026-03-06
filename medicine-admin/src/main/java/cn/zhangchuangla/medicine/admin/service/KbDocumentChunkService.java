package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface KbDocumentChunkService extends IService<KbDocumentChunk> {

    /**
     * 按文档ID替换切片数据（先删后插）。
     *
     * @param documentId 文档ID
     * @param chunks     新切片列表
     */
    void replaceByDocumentId(Long documentId, List<KbDocumentChunk> chunks);

    /**
     * 按文档ID集合批量删除切片。
     *
     * @param documentIds 文档ID集合
     * @return 删除结果
     */
    boolean removeByDocumentIds(List<Long> documentIds);

}
