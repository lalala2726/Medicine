package cn.zhangchuangla.medicine.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户收货地址表
 */
@TableName(value = "user_address")
@Data
public class UserAddress {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联user表
     */
    private Long userId;

    /**
     * 收件人姓名
     */
    private String consignee;

    /**
     * 收件人手机号
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址（街道/门牌号）
     */
    private String detail;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 是否为默认地址（0-否，1-是）
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除时间（逻辑删除用）
     */
    private Date deleteTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDelete;
}
