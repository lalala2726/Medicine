package cn.zhangchuangla.medicine.common.core.model.request.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 药品添加请求对象
 *
 * @author Chuang
 * created on 2025/9/22 13:46
 */
@Data
@Schema(description = "药品添加请求对象")
public class MedicineAddRequest {

    /**
     * 分类ID，关联 medicine_category
     */
    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    /**
     * 药品名称
     */
    @NotBlank(message = "药品名称不能为空")
    @Schema(description = "药品名称", type = "string", example = "阿莫西林", requiredMode = Schema.RequiredMode.REQUIRED)
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
    @Schema(description = "生产厂家", type = "string", example = "10")
    private Long supplierId;

    /**
     * 是否处方药（0-否，1-是）
     */
    @Schema(description = "是否处方药（0-否，1-是）", type = "int", example = "1")
    private Integer prescription = 0;

    /**
     * 状态（0-下架，1-上架）
     */
    @Schema(description = "状态（0-下架，1-上架）", type = "int", example = "1")
    private Integer status = 1;

    /**
     * 药品说明书/描述
     */
    @Schema(description = "药品说明书/描述", type = "string", example = "本品为青霉素类抗生素")
    private String description;

    /**
     * 药品图片URL列表
     */
    @Schema(description = "药品图片URL列表", type = "array", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> imageUrls;

}
