package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.mall.product.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
public interface MallProductMapper extends BaseMapper<MallProduct> {

    /**
     * 获取商城商品列表
     *
     * @param page    分页参数
     * @param request 查询参数
     * @return 分页的商城商品列表
     */
    Page<MallProduct> listMallProduct(Page<MallProduct> page, @Param("request") MallProductListQueryRequest request);

    /**
     * 获取商城商品列表（包含分类名称）
     *
     * @param page    分页参数
     * @param request 查询参数
     * @return 分页的商城商品列表（包含分类名称）
     */
    Page<MallProductDto> listMallProductWithCategory(Page<MallProductDto> page, @Param("request") MallProductListQueryRequest request);

    /**
     * 根据ID获取商城商品详情（包含药品及库存信息）
     *
     * @param id 商品ID
     * @return 商城商品详情
     */
    MallProductDetailDto getMallProductDetailById(@Param("id") Long id);
}

