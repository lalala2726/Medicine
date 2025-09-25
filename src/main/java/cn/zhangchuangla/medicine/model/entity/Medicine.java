package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 药品信息表
 */
@TableName(value = "medicine")
@Data
public class Medicine {

    /**
     * 药品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类ID，关联 medicine_category
     */
    private Long categoryId;

    /**
     * 药品名称
     */
    private String name;

    /**
     * 通用名
     */
    private String genericName;

    /**
     * 批准文号
     */
    private String approvalNumber;

    /**
     * 规格（例如：500mg*20片）
     */
    private String specification;

    /**
     * 生产厂家ID
     */
    private Long supplierId;

    /**
     * 是否处方药（0-否，1-是）
     */
    private Integer prescription;

    /**
     * 状态（0-下架，1-上架）
     */
    private Integer status;

    /**
     * 药品说明书/描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
