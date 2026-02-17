package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MallProductMapper extends BaseMapper<MallProduct> {

    /**
     * 商品列表（含分类与药品详情字段）。
     */
    Page<MallProductDetailDto> listMallProductWithCategory(Page<MallProductDetailDto> page,
                                                           @Param("request") MallProductListQueryRequest request);

    /**
     * 根据 ID 列表查询商品详情。
     */
    List<MallProductDetailDto> getMallProductDetailByIds(@Param("ids") List<Long> ids);
}
