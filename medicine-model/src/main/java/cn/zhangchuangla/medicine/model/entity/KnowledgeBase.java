package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 知识库表，存储所有知识库的元数据
 */
@TableName(value = "knowledge_base")
@Data
public class KnowledgeBase {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 知识库名称，不能为空
     */
    private String name;

    /**
     * 封面
     */
    private String cover;

    /**
     * 知识库描述，存储较长文本的描述
     */
    private String description;

    /**
     * 创建时间，默认为当前时间
     */
    private Date createTime;

    /**
     * 更新时间，默认为当前时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;
}
