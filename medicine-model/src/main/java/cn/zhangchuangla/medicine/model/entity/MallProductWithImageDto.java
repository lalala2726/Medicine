package cn.zhangchuangla.medicine.model.entity;

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
}
