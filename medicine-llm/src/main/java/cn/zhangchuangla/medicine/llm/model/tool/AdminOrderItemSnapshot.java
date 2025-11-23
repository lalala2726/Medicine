package cn.zhangchuangla.medicine.llm.model.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单子项简要信息。
 */
@Data
@Schema(description = "订单明细快照")
public class AdminOrderItemSnapshot {

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "小计金额")
    private BigDecimal totalPrice;

    @Schema(description = "售后状态，如 NONE/IN_PROGRESS/COMPLETED")
    private String afterSaleStatus;

    @Schema(description = "商品图片链接列表")
    private List<String> images;
}
