package cn.zhangchuangla.medicine.common.core.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * LLM配置表
 */
@TableName(value = "llm_config")
@Data
public class LlmConfig {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型提供商名称
     */
    @TableField(value = "provider")
    private String provider;

    /**
     * 模型
     */
    @TableField(value = "model")
    private String model;

    /**
     * API KEY
     */
    private String apiKey;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 删除时间
     */
    private Date deleteTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}
