package cn.zhangchuangla.medicine.common.core.model.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城商品运费配置视图对象
 *
 * @author Chuang
 * created on 2025/10/4 02:19
 */
@Data
@Schema(description = "商城商品运费配置视图对象")
public class MallProductShippingVo {

    /**
     * 运费模板ID
     */
    @Schema(description = "运费模板ID", type = "int64", example = "1")
    private Long id;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", type = "string", example = "包邮模板")
    private String name;

    /**
     * 运费类型（free-包邮，fixed-固定）
     */
    @Schema(description = "运费类型（free-包邮，fixed-固定）", type = "string", example = "free")
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

}
