package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.AfterSaleListRequest;
import cn.zhangchuangla.medicine.model.entity.MallAfterSale;
import cn.zhangchuangla.medicine.model.vo.AfterSaleListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 售后申请Mapper
 *
 * @author Chuang
 * created 2025/11/08
 */
@Mapper
public interface MallAfterSaleMapper extends BaseMapper<MallAfterSale> {

    /**
     * 分页查询售后列表(管理端)
     *
     * @param page    分页对象
     * @param request 查询条件
     * @return 售后列表
     */
    Page<AfterSaleListVo> selectAfterSaleList(@Param("page") Page<AfterSaleListVo> page,
                                              @Param("request") AfterSaleListRequest request);
}

