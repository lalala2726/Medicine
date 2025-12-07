package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.KbDocumentMapper;
import cn.zhangchuangla.medicine.admin.model.request.DocumentListRequest;
import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Chuang
 */
@Service
@RequiredArgsConstructor
public class KbDocumentServiceImpl extends ServiceImpl<KbDocumentMapper, KbDocument>
        implements KbDocumentService {

    private final KbDocumentMapper kbDocumentMapper;


    @Override
    public Page<KbDocument> documentPage(Integer id, DocumentListRequest request) {
        Page<KbDocument> page = request.toPage();
        return kbDocumentMapper.documentPage(id, request, page);
    }

    @Override
    public KbDocument getDocumentById(Long documentId) {
        Assert.isPositive(documentId, "文档ID不能为空");
        KbDocument document = getById(documentId);
        if (document == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "文档不存在");
        }
        return document;
    }
}




