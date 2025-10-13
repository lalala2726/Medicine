package cn.zhangchuangla.medicine.model.dto;

import cn.zhangchuangla.medicine.model.entity.MallProduct;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class MallProductDto extends MallProduct {

    /**
     * 所属分类
     */
    private String categoryName;

    /**
     * 商品展示图
     */
    private String image;
}
