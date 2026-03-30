package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.MallProductTagRel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品标签关联服务。
 *
 * @author Chuang
 */
public interface MallProductTagRelService extends IService<MallProductTagRel> {

    /**
     * 替换商品绑定的标签集合。
     *
     * @param productId 商品ID
     * @param tagIds    标签ID集合
     */
    void replaceProductTags(Long productId, List<Long> tagIds);

    /**
     * 按商品ID列表删除关联。
     *
     * @param productIds 商品ID列表
     */
    void removeByProductIds(List<Long> productIds);

    /**
     * 按商品ID列表查询标签关联。
     *
     * @param productIds 商品ID列表
     * @return 标签关联列表
     */
    List<MallProductTagRel> listByProductIds(List<Long> productIds);

    /**
     * 判断标签是否已绑定商品。
     *
     * @param tagId 标签ID
     * @return true-已绑定，false-未绑定
     */
    boolean existsByTagId(Long tagId);
}
