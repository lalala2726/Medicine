package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.request.mall.MallProductImageAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductImageListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductImageUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商城商品图片服务接口
 * <p>
 * 提供商城商品图片的业务逻辑处理，包括图片的增删改查、
 * 图片排序、商品关联等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:38
 */
public interface MallProductImageService extends IService<MallProductImage> {

    /**
     * 根据商品ID获取图片列表
     *
     * @param productId 商品ID
     * @return 图片列表
     */
    List<MallProductImage> listImagesByProductId(Long productId);

    /**
     * 分页查询商品图片列表
     *
     * @param request 查询参数
     * @return 分页图片列表
     */
    Page<MallProductImage> listImagesByProductId(MallProductImageListQueryRequest request);

    /**
     * 添加商品图片
     *
     * @param request 添加参数
     * @return 添加结果
     */
    boolean addImage(MallProductImageAddRequest request);

    /**
     * 修改商品图片
     *
     * @param request 修改参数
     * @return 修改结果
     */
    boolean updateImage(MallProductImageUpdateRequest request);

    /**
     * 删除商品图片
     *
     * @param ids 图片ID列表
     * @return 删除结果
     */
    boolean deleteImage(List<Long> ids);

}
