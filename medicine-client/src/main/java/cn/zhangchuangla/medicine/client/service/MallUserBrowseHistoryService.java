package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.MallUserBrowseHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface MallUserBrowseHistoryService extends IService<MallUserBrowseHistory> {

    /**
     * 添加商品浏览记录
     * @param userId 用户ID
     * @param productId 商品ID
     */
    void recordProductBrowse(Long userId, Long productId);

    /**
     * 添加商品浏览记录并更新商品浏览量
     * @param userId 用户ID
     * @param productId 商品ID
     */
    void recordAndUpdateProductBrowse(Long userId, Long productId);

    /**
     * 判断用户是否已浏览过该商品
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否已浏览过该商品
     */
    boolean hasBrowsed(Long userId, Long productId);
}
