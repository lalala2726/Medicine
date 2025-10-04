package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.model.entity.MallProductDetail;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商城商品详情服务接口
 * <p>
 * 提供商城商品详情的业务逻辑处理，包括详情的增删改查、
 * 详情内容管理、商品关联等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:36
 */
public interface MallProductDetailService extends IService<MallProductDetail> {

    /**
     * 根据商品ID获取详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    MallProductDetail getDetailByProductId(Long productId);

    /**
     * 分页查询商品详情列表
     *
     * @param request 查询参数
     * @return 分页详情列表
     */
    Page<MallProductDetail> listDetailsByProductId(MallProductDetailListQueryRequest request);

    /**
     * 添加商城商品详情
     *
     * @param request 添加参数
     * @return 添加结果
     */
    boolean addDetail(MallProductDetailAddRequest request);

    /**
     * 修改商城商品详情
     *
     * @param request 修改参数
     * @return 修改结果
     */
    boolean updateDetail(MallProductDetailUpdateRequest request);

    /**
     * 删除商城商品详情
     *
     * @param ids 详情ID列表
     * @return 删除结果
     */
    boolean deleteDetail(List<Long> ids);

}
