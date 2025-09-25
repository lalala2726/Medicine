package cn.zhangchuangla.medicine.model.vo.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 药品详情视图对象
 *
 * @author Chuang
 * created on 2025/9/22 13:48
 */
@Data
@Schema(description = "药品详情视图对象")
public class MedicineVo {

    /**
     * 药品ID
     */
    @Schema(description = "药品ID", type = "int64", example = "1")
    private Long id;

    /**
     * 分类ID，关联 medicine_category
     */
    @Schema(description = "分类ID", type = "int64", example = "1")
    private Long categoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", type = "string", example = "抗生素")
    private String categoryName;

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
    @Schema(description = "生产厂家", type = "string", example = "10")
    private Long supplierId;

    /**
     * 是否处方药（0-否，1-是）
     */
    @Schema(description = "是否处方药（0-否，1-是）", type = "int", example = "1")
    private Integer prescription;

    /**
     * 是否处方药名称
     */
    @Schema(description = "是否处方药名称", type = "string", example = "处方药")
    private String prescriptionName;

    /**
     * 状态（0-下架，1-上架）
     */
    @Schema(description = "状态（0-下架，1-上架）", type = "int", example = "1")
    private Integer status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称", type = "string", example = "上架")
    private String statusName;

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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "date", example = "2025-01-01 00:00:00")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", type = "date", example = "2025-01-01 00:00:00")
    private Date updateTime;

    /**
     * 创建者
     */
    @Schema(description = "创建者", type = "string", example = "admin")
    private String createBy;

    /**
     * 更新者
     */
    @Schema(description = "更新者", type = "string", example = "admin")
    private String updateBy;

}
