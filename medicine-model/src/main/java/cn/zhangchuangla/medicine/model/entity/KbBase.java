package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 知识库主数据表
 */
@TableName(value = "kb_base")
@Data
public class KbBase {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库唯一名称（业务键）
     */
    private String knowledgeName;

    /**
     * 知识库展示名称
     */
    private String displayName;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 绑定的 Milvus 集合名称
     */
    private String milvusCollectionName;

    /**
     * 向量模型标识
     */
    private String embeddingModel;

    /**
     * 向量维度
     */
    private Integer embeddingDim;

    /**
     * 记录状态（0启用 1停用）
     */
    private Integer status;

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
