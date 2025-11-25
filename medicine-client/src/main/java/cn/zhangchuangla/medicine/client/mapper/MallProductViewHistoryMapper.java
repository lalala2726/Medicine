package cn.zhangchuangla.medicine.client.mapper;

import cn.zhangchuangla.medicine.client.model.vo.ViewHistoryVo;
import cn.zhangchuangla.medicine.model.entity.MallProductViewHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface MallProductViewHistoryMapper extends BaseMapper<MallProductViewHistory> {

    /**
     * 获取用户浏览记录（包含商品名称、封面、价格、销量）
     */
    Page<ViewHistoryVo> listViewHistory(Page<ViewHistoryVo> page, @Param("userId") Long userId);

}




