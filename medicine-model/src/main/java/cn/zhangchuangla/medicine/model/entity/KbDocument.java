package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库文件表，存储与知识库相关的文件信息
 */
@TableName(value = "kb_document")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KbDocument {

    /**
     * UUID 类型的 id，确保每个文件的唯一性
     */
    @TableId
    private Long id;

    /**
     * 关联的知识库 ID，外键关联 knowledge_base 表
     */
    private Integer kbId;

    /**
     * 原始文件名
     */
    private String filename;

    /**
     * 对象存储中的路径
     */
    private String filePath;

    /**
     * 文件类型，如 pdf、docx、xlsx 等
     */
    private String fileType;

    /**
     * 文件处理的状态
     */
    private String status;

    /**
     * 切片数量，表示文件被切分成多少部分
     */
    private Integer chunkCount;
}
