package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.MallUserBrowseHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * @author Chuang
 */
@Validated
public interface MallUserBrowseHistoryService extends IService<MallUserBrowseHistory> {

    /**
     * 添加商品浏览记录
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    void recordProductBrowse(@NotNull(message = "用户ID不能为空") Long userId,
                             @NotNull(message = "商品ID不能为空") Long productId);


    /**
     * 判断用户是否已浏览过该商品
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否已浏览过该商品
     */
    boolean hasBrowsed(Long userId, Long productId);
}
