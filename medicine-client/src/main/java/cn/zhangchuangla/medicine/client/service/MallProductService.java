package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface MallProductService extends IService<MallProduct> {

    /**
     * 获取商品信息
     *
     * @param id 商品ID
     * @return 商品信息
     */
    MallProduct getMallProductById(Long id);


    /**
     * 记录商品浏览：累计浏览次数并刷新热度排行榜
     *
     * @param productId 商品ID
     */
    void recordView(Long productId);

    /**
     * 查询商品浏览量
     *
     * @param productId 商品ID
     * @param period    统计周期（为空时按总量返回）
     * @return 浏览次数
     */
    long getViewCount(Long productId, ProductViewPeriod period);
}
