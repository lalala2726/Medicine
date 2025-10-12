package cn.zhangchuangla.medicine.mapper;

import cn.zhangchuangla.medicine.model.dto.MallProductDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.MallProductListQueryRequest;
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
}




