package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 知识库文档元数据表
 */
@TableName(value = "kb_document")
@Data
public class KbDocument {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件 URL
     */
    private String fileUrl;

    /**
     * 文件内容 SHA-256 哈希
     */
    private String fileSha256;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件 MIME 类型
     */
    private String mimeType;

    /**
     * 预留文档版本字段
     */
    private Integer version;

    /**
     * 索引状态
     */
    private String status;

    /**
     * 最近一次处理失败错误信息
     */
    private String lastError;

    /**
     * 创建人账号
     */
    private String createBy;

    /**
     * 最后更新人账号
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;

    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;

    /**
     * 逻辑删除时间
     */
    private Date deletedAt;
}
