package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallUserBrowseHistoryMapper;
import cn.zhangchuangla.medicine.client.service.MallUserBrowseHistoryService;
import cn.zhangchuangla.medicine.model.entity.MallUserBrowseHistory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Chuang
 */
@Service
public class MallUserBrowseHistoryServiceImpl extends ServiceImpl<MallUserBrowseHistoryMapper, MallUserBrowseHistory>
        implements MallUserBrowseHistoryService {

    /**
     * 添加商品浏览记录
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    @Override
    public void recordProductBrowse(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return;
        }
        Date now = new Date();
        if (hasBrowsed(userId, productId)) {
            lambdaUpdate()
                    .eq(MallUserBrowseHistory::getUserId, userId)
                    .eq(MallUserBrowseHistory::getProductId, productId)
                    .set(MallUserBrowseHistory::getUpdateTime, now)
                    .update();
        } else {
            MallUserBrowseHistory mallUserBrowseHistory = MallUserBrowseHistory.builder()
                    .userId(userId)
                    .productId(productId)
                    .createTime(now)
                    .updateTime(now)
                    .build();
            save(mallUserBrowseHistory);
        }
    }

    /**
     * 判断用户是否已浏览过该商品
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否已浏览过该商品
     */
    @Override
    public boolean hasBrowsed(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        LambdaQueryWrapper<MallUserBrowseHistory> eq = new LambdaQueryWrapper<MallUserBrowseHistory>()
                .eq(MallUserBrowseHistory::getUserId, userId)
                .eq(MallUserBrowseHistory::getProductId, productId);
        return count(eq) > 0;
    }
}




