package cn.zhangchuangla.medicine.client.service;

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
}
