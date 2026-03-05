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
    void replaceByDocumentId(String documentId, List<KbDocumentChunk> chunks);

}
