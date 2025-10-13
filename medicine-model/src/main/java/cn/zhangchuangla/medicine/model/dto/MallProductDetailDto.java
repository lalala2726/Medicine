package cn.zhangchuangla.medicine.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商城商品详情数据传输对象
 *
 * <p>在商品绑定药品库存时提供额外的药品及库存批次信息。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MallProductDetailDto extends MallProductDto {

    /**
     * 关联药品名称
     */
    private String medicineName;

    /**
     * 关联药品库存批次号
     */
    private String batchNo;

    /**
     * 关联药品库存所在仓库
     */
    private String medicineWarehouse;

    /**
     * 商品图片列表
     */
    private List<String> images;

}
