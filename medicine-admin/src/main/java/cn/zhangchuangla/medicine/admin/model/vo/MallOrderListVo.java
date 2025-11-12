package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31
 */
@Data
@Schema(description = "订单列表视图对象")
public class MallOrderListVo {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单编号（业务唯一标识）", example = "O202510312122")
    private String orderNo;

    @Schema(description = "订单总金额（含运费）", example = "100.00")
    private BigDecimal totalAmount;

    @Schema(description = "支付方式编码", example = "ALIPAY")
    private String payType;

    @Schema(description = "订单状态编码", example = "WAIT_PAY")
    private String orderStatus;

    @Schema(description = "支付时间", example = "2025-10-31 21:22:00")
    private Date payTime;

    @Schema(description = "创建时间", example = "2025-10-31 21:22:00")
    private Date createTime;

    @Schema(description = "商品信息")
    private ProductInfo productInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductInfo {

        @Schema(description = "商品名称", example = "商品名称")
        private String productName;

        @Schema(description = "商品图片", example = "商品图片")
        private String productImage;

        @Schema(description = "商品数量", example = "1")
        private Integer quantity;
    }

}
