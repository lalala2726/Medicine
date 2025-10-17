package cn.zhangchuangla.medicine.admin.model.vo.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 供应商列表展示对象
 */
@Data
@Schema(description = "供应商列表展示对象")
public class SupplierListVo {

    /**
     * 供应商ID
     */
    @Schema(description = "供应商ID", type = "int64", example = "1")
    private Long id;

    /**
     * 供应商名称
     */
    @Schema(description = "供应商名称", type = "string", example = "华北医药供应商")
    private String name;

    /**
     * 联系人
     */
    @Schema(description = "联系人", type = "string", example = "张三")
    private String contact;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", type = "string", example = "13800000000")
    private String phone;

    /**
     * 供应商地址
     */
    @Schema(description = "供应商地址", type = "string", example = "北京市朝阳区XX路")
    private String address;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "string", format = "date-time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", type = "string", format = "date-time")
    private Date updateTime;
}
