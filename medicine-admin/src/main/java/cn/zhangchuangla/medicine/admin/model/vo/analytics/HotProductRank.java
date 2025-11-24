package cn.zhangchuangla.medicine.admin.model.vo.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "热销商品排行")
public class HotProductRank {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "售出数量")
    private Long quantity;

    @Schema(description = "销售额")
    private BigDecimal amount;

    @Schema(description = "累计销量（存量字段）")
    private Long salesVolume;
}
