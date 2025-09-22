package cn.zhangchuangla.medicine.model.request.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 药品更新请求对象
 *
 * @author Chuang
 * created on 2025/9/22 13:47
 */
@Data
@Schema(description = "药品更新请求对象")
public class MedicineUpdateRequest {

    /**
     * 药品ID
     */
    @NotNull(message = "药品ID不能为空")
    @Schema(description = "药品ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 分类ID，关联 medicine_category
     */
    @Schema(description = "分类ID", type = "int64", example = "1")
    private Long categoryId;

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
     * 批准文号
     */
    @Schema(description = "批准文号", type = "string", example = "国药准字H20033211")
    private String approvalNumber;

    /**
     * 规格（例如：500mg*20片）
     */
    @Schema(description = "规格", type = "string", example = "500mg*20片")
    private String specification;

    /**
     * 生产厂家
     */
    @Schema(description = "生产厂家", type = "string", example = "哈药集团")
    private String manufacturer;

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

    /**
     * 药品说明书/描述
     */
    @Schema(description = "药品说明书/描述", type = "string", example = "本品为青霉素类抗生素")
    private String description;

}
