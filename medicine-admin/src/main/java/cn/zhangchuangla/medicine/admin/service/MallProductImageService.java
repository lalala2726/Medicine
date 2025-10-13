package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 商城商品图片服务接口
 * <p>
 * 提供商城商品图片的业务逻辑处理，包括图片的增删改查、
 * 图片排序、商品关联等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:38
 */
@Validated
public interface MallProductImageService extends IService<MallProductImage> {


    /**
     * 添加商品图片
     *
     * @param images 图片列表
     * @param id     商品ID
     */
    void addProductImages(@NotEmpty(message = "图片列表不能为空") List<String> images,
                          @NotNull(message = "商品ID不能为空") Long id);

    /**
     * 更新商品图片
     *
     * @param images 图片列表
     * @param id     商品ID
     */
    void updateProductImageById(@NotEmpty(message = "商品图片列表不能为空") List<String> images,
                                @NotNull(message = "商品ID不能为空") Long id);

    /**
     * 删除商品图片
     *
     * @param ids 商品ID列表
     */
    void removeImagesById(@NotEmpty(message = "商品ID列表不能为空") List<Long> ids);
}
