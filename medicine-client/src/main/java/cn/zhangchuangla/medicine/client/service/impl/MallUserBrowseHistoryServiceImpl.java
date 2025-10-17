package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallUserBrowseHistoryMapper;
import cn.zhangchuangla.medicine.client.service.MallUserBrowseHistoryService;
import cn.zhangchuangla.medicine.model.entity.MallUserBrowseHistory;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author Chuang
 */
@Service
public class MallUserBrowseHistoryServiceImpl extends ServiceImpl<MallUserBrowseHistoryMapper, MallUserBrowseHistory>
        implements MallUserBrowseHistoryService {

    @Override
    public void recordProductBrowse(Long userId, Long productId) {

    }

    @Override
    public void recordAndUpdateProductBrowse(Long userId, Long productId) {

    }

    @Override
    public boolean hasBrowsed(Long userId, Long productId) {
        return false;
    }
}




