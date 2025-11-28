package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.enums.ProductViewPeriod;
import cn.zhangchuangla.medicine.client.model.vo.MallProductSearchVo;
import cn.zhangchuangla.medicine.client.model.vo.MallProductVo;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.elasticsearch.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.model.dto.MallProductWithImageDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.vo.mall.RecommendListVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品服务接口（客户端）
 *
 * @author Chuang
 */
public interface MallProductService extends IService<MallProduct> {


    /**
     * 推荐商品
     *
     * @return 推荐结果
     */
    List<RecommendListVo> recommend();

    /**
     * 获取商品信息
     *
     * @param id 商品ID
     * @return 商品信息
     */
    MallProduct getMallProductById(Long id);

    /**
     * 获取商品详情（包含图片和药品详情）
     *
     * @param id 商品ID
     * @return 商品详情VO
     */
    MallProductVo getMallProductDetail(Long id);

    /**
     * 获取商品信息（包含图片）
     *
     * @param id 商品ID
     * @return 商品信息
     */
    MallProductWithImageDto getProductWithImagesById(Long id);


    /**
     * 记录商品浏览：累计浏览次数并刷新热度排行榜
     *
     * @param productId 商品ID
     */
    void recordView(Long productId);

    /**
     * 查询商品浏览量
     *
     * @param productId 商品ID
     * @param period    统计周期（为空时按总量返回）
     * @return 浏览次数
     */
    long getViewCount(Long productId, ProductViewPeriod period);


    /**
     * 扣减库存
     *
     * @param productId 商品ID
     * @param quantity  数量
     */
    void deductStock(Long productId, Integer quantity);

    /**
     * 恢复库存
     *
     * @param productId 商品ID
     * @param quantity  数量
     */
    void restoreStock(Long productId, Integer quantity);

    /**
     * 搜索商品（名称/品牌/功效等）。
     *
     * @return 搜索结果
     */
    PageResult<MallProductSearchVo> search(MallProductSearchRequest request);

    /**
     * 搜索建议（商品名/分类名/通用名）。
     *
     * @param keyword 关键字
     * @return 建议列表
     */
    List<String> suggest(String keyword);

}
