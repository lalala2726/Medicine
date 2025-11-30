package cn.zhangchuangla.medicine.client.mapper;

import cn.zhangchuangla.medicine.client.model.dto.RecommendProductDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductWithImageDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 获取商品详情（含药品详情、封面）
     */
    MallProductDetailDto getProductAndDrugInfoById(@Param("productId") Long productId);

    /**
     * 根据销量与浏览量推荐商品
     */
    List<RecommendProductDto> listRecommendProducts();
}




