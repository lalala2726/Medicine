package cn.zhangchuangla.medicine.model.request.mall.shipping;

import cn.zhangchuangla.medicine.common.core.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商城商品运费配置列表查询请求对象
 *
 * @author Chuang
 * created on 2025/10/4 14:20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商城商品运费配置列表查询请求对象")
public class MallProductShippingListQueryRequest extends BasePageRequest {

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", type = "string", example = "包邮模板")
    private String name;

    /**
     * 运费类型（free-包邮，fixed-固定）
     */
    @Schema(description = "运费类型", type = "string", example = "free")
    private String type;

    /**
     * 运费价格
     */
    @Schema(description = "运费价格", type = "number", example = "10.00")
    private BigDecimal price;

}
