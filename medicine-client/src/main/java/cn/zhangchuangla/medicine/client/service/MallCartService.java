package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.MallCart;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.validation.annotation.Validated;

/**
 * @author Chuang
 */
@Validated
public interface MallCartService extends IService<MallCart> {

    /**
     * 添加商品到购物车
     *
     * @param productId 商品ID
     * @param quantity  添加数量，默认为1
     * @return 是否添加成功
     */
    boolean addProduct(Long productId, Integer quantity);

    /**
     * 添加商品到购物车（默认数量为1）
     *
     * @param productId 商品ID
     * @return 是否添加成功
     */
    default boolean addProduct(Long productId) {
        return addProduct(productId, 1);
    }
}
