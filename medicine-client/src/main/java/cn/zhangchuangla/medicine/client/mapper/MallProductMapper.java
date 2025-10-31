package cn.zhangchuangla.medicine.client.mapper;

import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.entity.MallProductWithImageDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface MallProductMapper extends BaseMapper<MallProduct> {

    /**
     * 根据乐观锁版本号更新库存
     *
     * @param productId 商品ID
     * @param quantity  数量
     * @param version   版本号
     * @return 更新记录数
     */
    int updateStockWithVersion(@Param("productId") Long productId, @Param("quantity") Integer quantity, @Param("version") Integer version);

    /**
     * 根据商品ID获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    MallProductWithImageDto getProductWithImagesById(@Param("productId") Long productId);
}




