package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.MallProductShipping;
import cn.zhangchuangla.medicine.model.request.mall.Shipping.MallProductShippingAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.Shipping.MallProductShippingListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.Shipping.MallProductShippingUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商城商品运费配置服务接口
 * <p>
 * 提供商城商品运费模板的业务逻辑处理，包括运费模板的增删改查、
 * 运费类型管理、价格配置等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:14
 */
public interface MallProductShippingService extends IService<MallProductShipping> {

    /**
     * 分页查询商品运费配置列表
     *
     * @param request 查询参数
     * @return 分页运费配置列表
     */
    Page<MallProductShipping> listShippingsByProductId(MallProductShippingListQueryRequest request);

    /**
     * 添加运费模板
     *
     * @param request 添加参数
     * @return 添加结果
     */
    boolean addShipping(MallProductShippingAddRequest request);

    /**
     * 修改运费模板
     *
     * @param request 修改参数
     * @return 修改结果
     */
    boolean updateShipping(MallProductShippingUpdateRequest request);

    /**
     * 删除运费模板
     *
     * @param ids 运费模板ID列表
     * @return 删除结果
     */
    boolean deleteShipping(List<Long> ids);

}
