package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
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
    Page<MallProductDetailDto> listMallProductWithCategory(Page<MallProductDetailDto> page, @Param("request") MallProductListQueryRequest request);

    /**
     * 根据ID获取商城商品详情（包含图片信息）
     *
     * @param id 商品ID
     * @return 商城商品详情
     */
    MallProductDetailDto getMallProductDetailById(@Param("id") Long id);

    /**
     * 分批读取上架商品，用于批量同步索引（避免一次性拉取全量数据）。
     *
     * @param lastId 上一次读取的最后一条商品ID
     * @param limit  本次读取数量
     * @return 商品详情列表
     */
    java.util.List<MallProductDetailDto> listOnShelfForIndex(@Param("lastId") Long lastId, @Param("limit") int limit);
}
