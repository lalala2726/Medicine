package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * @author Chuang
 */
@Validated
public interface MallProductImageService extends IService<MallProductImage> {

    /**
     * 获取商品封面图片
     *
     * @param productId 商品ID
     * @return 商品封面图片
     */
    String getProductCoverImage(@NotNull(message = "商品ID不能为空") Long productId);
}
