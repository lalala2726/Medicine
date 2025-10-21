package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallUserBrowseHistoryMapper;
import cn.zhangchuangla.medicine.client.service.MallUserBrowseHistoryService;
import cn.zhangchuangla.medicine.model.entity.MallUserBrowseHistory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
        MallUserBrowseHistory mallUserBrowseHistory = MallUserBrowseHistory.builder()
                .userId(userId)
                .productId(productId)
                .build();
        LambdaQueryChainWrapper<MallUserBrowseHistory> eq = lambdaQuery()
                .eq(MallUserBrowseHistory::getUserId, userId)
                .eq(MallUserBrowseHistory::getProductId, productId);

        if (hasBrowsed(userId, productId)) {
            update(mallUserBrowseHistory, eq);
        } else {
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
        LambdaQueryWrapper<MallUserBrowseHistory> eq = new LambdaQueryWrapper<MallUserBrowseHistory>()
                .eq(MallUserBrowseHistory::getUserId, userId)
                .eq(MallUserBrowseHistory::getProductId, productId);
        return count(eq) > 0;
    }
}




