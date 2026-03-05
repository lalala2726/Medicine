package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.KbDocumentChunkMapper;
import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.entity.KbDocumentChunk;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Chuang
 */
@Service
public class KbDocumentChunkServiceImpl extends ServiceImpl<KbDocumentChunkMapper, KbDocumentChunk>
        implements KbDocumentChunkService {

    private static final int BATCH_SIZE = 500;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceByDocumentId(String documentId, List<KbDocumentChunk> chunks) {
        Assert.notEmpty(documentId, "文档ID不能为空");

        lambdaUpdate()
                .eq(KbDocumentChunk::getDocumentId, documentId)
                .remove();

        if (CollectionUtils.isEmpty(chunks)) {
            return;
        }
        boolean saved = saveBatch(chunks, BATCH_SIZE);
        if (!saved) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "保存文档切片失败");
        }
    }
}
