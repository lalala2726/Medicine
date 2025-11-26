package cn.zhangchuangla.medicine.model.dto;

import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MallProductWithImageDto extends MallProduct {

    /**
     * 商品图片列表
     */
    List<MallProductImage> productImages;

    /**
     * 药品详细信息
     */
    private DrugDetailDto drugDetail;

    /**
     * 商品销量（已完成订单数量汇总）
     */
    private Integer sales;
}
