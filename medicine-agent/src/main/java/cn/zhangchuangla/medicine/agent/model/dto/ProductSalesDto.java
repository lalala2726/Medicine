package cn.zhangchuangla.medicine.agent.model.dto;

import lombok.Data;

@Data
public class ProductSalesDto {

    /**
     * 商品 ID。
     */
    private Long productId;

    /**
     * 销量。
     */
    private Integer sales;
}
