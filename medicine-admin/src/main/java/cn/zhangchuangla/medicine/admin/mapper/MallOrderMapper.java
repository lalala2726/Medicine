package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface MallOrderMapper extends BaseMapper<MallOrder> {

    Page<MallOrder> orderList(Page<MallOrder> mallOrderPage, @Param("request") MallOrderListRequest request);
}




