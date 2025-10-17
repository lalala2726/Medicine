package cn.zhangchuangla.medicine.model.request.mall.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16 16:16
 */
@Data
@Schema(description = "商品推荐请求参数")
public class RecommendRequest {


    /**
     * 分页大小
     */
    @Schema(description = "分页大小", type = "int", format = "int32", example = "10")
    private Integer size;

    /**
     * 热门游标
     */
    @Schema(description = "热门游标", type = "int", format = "int32", example = "0")
    private Integer hotCursor;

}
