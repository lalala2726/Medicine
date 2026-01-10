package cn.zhangchuangla.medicine.client.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商品搜索请求参数")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MallProductSearchRequest extends PageRequest {

    @Schema(description = "搜索关键字,这个关键字匹配商品名称、商品分类名称、商品描述、厂商名称、药品通用名、功效/主治", example = "感冒灵")
    private String keyword;

    @Schema(description = "商品名称", example = "999感冒灵颗粒")
    private String name;

    @Schema(description = "商品分类名称", example = "感冒药")
    private String categoryName;

    @Schema(description = "商品价格", example = "29.90")
    private BigDecimal price;

    @Schema(description = "最低价格", example = "10.00")
    private BigDecimal minPrice;

    @Schema(description = "最高价格", example = "99.00")
    private BigDecimal maxPrice;

    @Schema(description = "价格排序方向（asc/desc）", example = "asc")
    private String priceSort;

    @Schema(description = "销量排序方向（asc/desc）", example = "desc")
    private String salesSort;

    @Schema(description = "商品状态", example = "1")
    private Integer status;

    @Schema(description = "厂商名称", example = "华润三九医药股份有限公司")
    private String brand;

    @Schema(description = "药品通用名", example = "复方氨酚烷胺片")
    private String commonName;

    @Schema(description = "功效/主治", example = "用于缓解普通感冒及流行性感冒引起的发热、头痛、四肢酸痛、打喷嚏、流鼻涕、鼻塞、咽痛等症状")
    private String efficacy;

    public MallProductSearchRequest(String keyword, int limit) {
        this.keyword = keyword;
        super.setPageSize(limit);
    }
}
