package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 知识库文档切片详情表
 */
@TableName(value = "kb_document_chunk")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KbDocumentChunk {

    /**
     * 切片主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 唯一标识符
     */
    private String uuid;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 修改者
     */
    private String updateBy;

    /**
     * 创建者
     */
    private String createBy;


    /**
     * 关联的文件 ID，外键关联 kb_document
     */
    private Long docId;

    /**
     * 切片序号 (0, 1, 2...)，用于排序和展示上下文
     */
    private Integer chunkIndex;

    /**
     * 切片原文内容。使用 LONGTEXT 以容纳较长文本(如Excel转文本)
     */
    private String content;

    /**
     * 该切片的 Token 数量，用于估算 LLM 成本
     */
    private Integer tokenCount;

    /**
     * 来源页码 (针对 PDF/PPT/Word)，方便用户溯源
     */
    private Integer pageNum;

    /**
     * 关联 Milvus 中的 Vector ID，用于删除或更新向量
     */
    private Long vectorId;

    /**
     * 创建时间
     */
    private Date createTime;
}
