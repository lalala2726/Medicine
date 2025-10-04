package cn.zhangchuangla.medicine.model.request.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商城商品运费配置修改请求对象
 *
 * @author Chuang
 * created on 2025/10/4 02:29
 */
@Data
@Schema(description = "商城商品运费配置修改请求对象")
public class MallProductShippingUpdateRequest {

    /**
     * 运费模板ID
     */
    @NotNull(message = "运费模板ID不能为空")
    @Schema(description = "运费模板ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", type = "string", example = "包邮模板", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 运费类型（free-包邮，fixed-固定）
     */
    @NotBlank(message = "运费类型不能为空")
    @Schema(description = "运费类型（free-包邮，fixed-固定）", type = "string", example = "free", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    /**
     * 运费价格（type=fixed时生效）
     */
    @Schema(description = "运费价格", type = "number", example = "10.00")
    private BigDecimal price;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述", type = "string", example = "满99包邮")
    private String description;

}