package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.dto.KnowledgeBaseDocumentDto;
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
     * @param page            分页参数
     * @param knowledgeBaseId 知识库ID
     * @param request         查询条件
     * @return 文档分页结果
     */
    Page<KnowledgeBaseDocumentDto> listDocument(Page<KnowledgeBaseDocumentDto> page,
                                                @Param("knowledgeBaseId") Long knowledgeBaseId,
                                                @Param("request") DocumentListRequest request);

    /**
     * 根据ID查询知识库文档详情。
     *
     * @param id 文档ID
     * @return 文档详情
     */
    KnowledgeBaseDocumentDto getDocumentDetailById(@Param("id") Long id);
}
