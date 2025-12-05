package cn.zhangchuangla.medicine.admin.model.dto;

import cn.zhangchuangla.medicine.model.entity.KnowledgeBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 扩展知识库实体，包含文件与切片聚合统计
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseStatsDto extends KnowledgeBase {

    /**
     * 文件数量
     */
    private Integer fileCount;

    /**
     * 切片数量
     */
    private Integer sliceCount;
}
