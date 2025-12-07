package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.DocumentListRequest;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Chuang
 */
public interface KbDocumentMapper extends BaseMapper<KbDocument> {

    /**
     * 文档列表
     *
     * @param id      知识库ID
     * @param request 查询参数
     * @param page    分页参数
     * @return 文档列表
     */
    Page<KbDocument> documentPage(Integer id, DocumentListRequest request, Page<KbDocument> page);
}




