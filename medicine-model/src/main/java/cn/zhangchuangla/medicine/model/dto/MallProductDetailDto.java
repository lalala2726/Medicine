package cn.zhangchuangla.medicine.model.dto;

import cn.zhangchuangla.medicine.model.entity.MallProduct;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class MallProductDetailDto extends MallProduct {

    /**
     * 所属分类
     */
    private String categoryName;

    /**
     * 商品销量
     */
    private Integer sales;

    /**
     * 商品图片列表
     */
    private List<String> images;

    /**
     * 药品详细信息
     */
    private DrugDetailDto drugDetail;
}
