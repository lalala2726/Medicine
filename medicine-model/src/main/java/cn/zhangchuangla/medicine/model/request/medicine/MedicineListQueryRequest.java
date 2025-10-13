package cn.zhangchuangla.medicine.model.request.medicine;

import cn.zhangchuangla.medicine.common.core.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品列表查询请求对象
 *
 * @author Chuang
 * created on 2025/9/22 13:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "药品列表查询请求对象")
public class MedicineListQueryRequest extends BasePageRequest {

    /**
     * 药品ID
     */
    @Schema(description = "药品ID", type = "int64", example = "1")
    private Long id;

    /**
     * 药品名称
     */
    @Schema(description = "药品名称", type = "string", example = "阿莫西林")
    private String name;

    /**
     * 通用名
     */
    @Schema(description = "通用名", type = "string", example = "阿莫西林胶囊")
    private String genericName;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", type = "int64", example = "1")
    private Long categoryId;

    /**
     * 生产厂家ID
     */
    @Schema(description = "生产厂家ID", type = "string", example = "10")
    private Long supplierId;

    /**
     * 批准文号
     */
    @Schema(description = "批准文号", type = "string", example = "国药准字H20033211")
    private String approvalNumber;

    /**
     * 是否处方药（0-否，1-是）
     */
    @Schema(description = "是否处方药（0-否，1-是）", type = "int", example = "1")
    private Integer prescription;

    /**
     * 状态（0-下架，1-上架）
     */
    @Schema(description = "状态（0-下架，1-上架）", type = "int", example = "1")
    private Integer status;

}
