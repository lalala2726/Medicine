package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallProductImageMapper;
import cn.zhangchuangla.medicine.client.service.MallProductImageService;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * @author zhangchuang
 */
@Service
public class MallProductImageServiceImpl extends ServiceImpl<MallProductImageMapper, MallProductImage>
        implements MallProductImageService {

    /**
     * 获取商品封面图片
     *
     * @param productId 商品ID
     * @return 商品封面图片
     */
    @Override
    public String getProductCoverImage(Long productId) {
        LambdaQueryChainWrapper<MallProductImage> eq = lambdaQuery().eq(MallProductImage::getProductId, productId);
        List<MallProductImage> list = list(eq);
        return list.stream()
                .min(Comparator.comparingInt(MallProductImage::getSort))
                .map(MallProductImage::getImageUrl)
                .orElse("");
    }
}




