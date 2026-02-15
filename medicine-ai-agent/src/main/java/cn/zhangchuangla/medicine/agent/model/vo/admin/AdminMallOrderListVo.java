package cn.zhangchuangla.medicine.agent.model.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Admin 端智能体订单列表视图。
 */
@Schema(description = "Admin 端智能体订单列表")
public class AdminMallOrderListVo {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public static class ProductInfo {

        @Schema(description = "商品名称", example = "商品名称")
        private String productName;

        @Schema(description = "商品图片", example = "商品图片")
        private String productImage;

        @Schema(description = "商品价格", example = "100.00")
        private BigDecimal productPrice;

        @Schema(description = "商品分类")
        private String productCategory;

        @Schema(description = "商品ID", example = "1")
        private Long productId;

        @Schema(description = "商品数量", example = "1")
        private Integer quantity;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductImage() {
            return productImage;
        }

        public void setProductImage(String productImage) {
            this.productImage = productImage;
        }

        public BigDecimal getProductPrice() {
            return productPrice;
        }

        public void setProductPrice(BigDecimal productPrice) {
            this.productPrice = productPrice;
        }

        public String getProductCategory() {
            return productCategory;
        }

        public void setProductCategory(String productCategory) {
            this.productCategory = productCategory;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
