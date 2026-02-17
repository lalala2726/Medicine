package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 Mapper 接口。
 * <p>
 * 提供商品数据的数据访问操作，包括基础 CRUD 和自定义查询。
 *
 * @author Chuang
 */
@Mapper
public interface MallProductMapper extends BaseMapper<MallProduct> {

    /**
     * 分页查询商品列表（含分类与药品详情字段）。
     *
     * @param page    分页参数
     * @param request 查询请求参数
     * @return 商品详情分页数据
     */
    Page<MallProductDetailDto> listMallProductWithCategory(Page<MallProductDetailDto> page,
                                                           @Param("request") MallProductListQueryRequest request);

    /**
     * 根据 ID 列表批量查询商品详情。
     *
     * @param ids 商品 ID 列表
     * @return 商品详情列表
     */
    List<MallProductDetailDto> getMallProductDetailByIds(@Param("ids") List<Long> ids);
}
