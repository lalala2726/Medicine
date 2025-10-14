package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.MallProductSku;
import cn.zhangchuangla.medicine.model.request.mall.sku.MallProductSkuAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.sku.MallProductSkuListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.sku.MallProductSkuUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商城商品SKU规格服务接口
 * <p>
 * 提供商城商品SKU规格的业务逻辑处理，包括SKU的增删改查、
 * 规格管理、库存配置、价格管理等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:17
 */
public interface MallProductSkuService extends IService<MallProductSku> {

    /**
     * 根据商品ID获取SKU列表
     *
     * @param productId 商品ID
     * @return SKU列表
     */
    List<MallProductSku> listSkusByProductId(Long productId);

    /**
     * 分页查询商品SKU列表
     *
     * @param request 查询参数
     * @return 分页SKU列表
     */
    Page<MallProductSku> listSkusByProductId(MallProductSkuListQueryRequest request);

    /**
     * 添加商品SKU
     *
     * @param request 添加参数
     * @return 添加结果
     */
    boolean addSku(MallProductSkuAddRequest request);

    /**
     * 修改商品SKU
     *
     * @param request 修改参数
     * @return 修改结果
     */
    boolean updateSku(MallProductSkuUpdateRequest request);

    /**
     * 删除商品SKU
     *
     * @param ids SKU ID列表
     * @return 删除结果
     */
    boolean deleteSku(List<Long> ids);

}
