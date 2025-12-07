package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.KbDocumentChunkMapper;
import cn.zhangchuangla.medicine.admin.model.request.DocumentSliceListRequest;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class KbDocumentChunkServiceImpl extends ServiceImpl<KbDocumentChunkMapper, KbDocumentChunk>
        implements KbDocumentChunkService {

    private final KbDocumentChunkMapper kbDocumentChunkMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveKbDocuments(List<KbDocumentChunk> chunkEntities) {
        return saveBatch(chunkEntities);
    }

    @Override
    public Page<KbDocumentChunk> documentSliceList(Long documentId, DocumentSliceListRequest request) {
        Page<KbDocumentChunk> page = request.toPage();
        return kbDocumentChunkMapper.selectDocumentSlices(page, documentId, request.getName());
    }
}




