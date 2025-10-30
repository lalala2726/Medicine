package cn.zhangchuangla.medicine.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商城商品详情数据传输对象。
 * <p>
 * 聚焦商城商品本身及其图片信息，便于后台管理端渲染详情页。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MallProductDetailDto extends MallProductDto {

    /**
     * 商品图片列表
     */
    private List<String> images;

}
