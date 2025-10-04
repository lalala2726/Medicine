package cn.zhangchuangla.medicine.model.request.mall;

import cn.zhangchuangla.medicine.common.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商城商品详情列表查询请求对象
 *
 * @author Chuang
 * created on 2025/10/4 14:20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商城商品详情列表查询请求对象")
public class MallProductDetailListQueryRequest extends BasePageRequest {

    /**
     * 商品ID，关联 mall_product
     */
    @Schema(description = "商品ID", type = "int64", example = "1")
    private Long productId;

}