package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.DocumentListRequest;
import cn.zhangchuangla.medicine.model.entity.KbDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface KbDocumentMapper extends BaseMapper<KbDocument> {

    /**
     * 分页查询知识库文档列表。
     *
     * @param page 分页参数
     * @param knowledgeBaseId 知识库ID
     * @param request 查询条件
     * @return 文档分页结果
     */
    Page<KbDocument> listDocument(Page<KbDocument> page,
                                  @Param("knowledgeBaseId") Long knowledgeBaseId,
                                  @Param("request") DocumentListRequest request);
}




