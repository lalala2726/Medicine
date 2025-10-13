package cn.zhangchuangla.medicine.model.request.medicine;

import cn.zhangchuangla.medicine.common.core.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商列表查询请求对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "供应商列表查询请求对象")
public class SupplierListQueryRequest extends BasePageRequest {

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
}
