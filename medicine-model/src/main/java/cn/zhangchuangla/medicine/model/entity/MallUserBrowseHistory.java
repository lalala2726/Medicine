package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户商品浏览历史表
 */
@TableName(value = "mall_user_browse_history")
@Data
public class MallUserBrowseHistory {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;


    /**
     * 访问IP
     */
    private String ipAddress;

    /**
     * 访问设备类型，如mobile、pc
     */
    private String device;

    /**
     * 停留时长(秒)
     */
    private Integer stayDuration;

    /**
     * 来源页面，如首页、分类页、搜索页
     */
    private String sourcePage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
