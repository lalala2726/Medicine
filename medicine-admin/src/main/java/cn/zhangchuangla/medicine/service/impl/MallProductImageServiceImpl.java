package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductImageAddRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductImageListQueryRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductImageUpdateRequest;
import cn.zhangchuangla.medicine.mapper.MallProductImageMapper;
import cn.zhangchuangla.medicine.service.MallProductImageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 商城商品图片服务实现类
 * <p>
 * 实现商城商品图片的业务逻辑处理，包括图片的增删改查、
 * 图片排序、商品关联等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:39
 */
@Service
public class MallProductImageServiceImpl extends ServiceImpl<MallProductImageMapper, MallProductImage>
        implements MallProductImageService {

    @Override
    public List<MallProductImage> listImagesByProductId(Long productId) {
        if (productId == null) {
            throw new ServiceException("商品ID不能为空");
        }

        LambdaQueryWrapper<MallProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallProductImage::getProductId, productId)
                .orderByAsc(MallProductImage::getSort)
                .orderByDesc(MallProductImage::getCreateTime);

        return list(queryWrapper);
    }

    @Override
    public Page<MallProductImage> listImagesByProductId(MallProductImageListQueryRequest request) {
        LambdaQueryWrapper<MallProductImage> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getProductId() != null) {
            queryWrapper.eq(MallProductImage::getProductId, request.getProductId());
        }

        if (StringUtils.hasText(request.getImageUrl())) {
            queryWrapper.like(MallProductImage::getImageUrl, request.getImageUrl());
        }

        if (request.getSort() != null) {
            queryWrapper.eq(MallProductImage::getSort, request.getSort());
        }

        queryWrapper.orderByAsc(MallProductImage::getSort)
                .orderByDesc(MallProductImage::getCreateTime);

        return page(new Page<>(request.getPageNum(), request.getPageSize()), queryWrapper);
    }

    @Override
    public boolean addImage(MallProductImageAddRequest request) {
        MallProductImage image = new MallProductImage();
        BeanUtils.copyProperties(request, image);
        image.setCreateTime(new Date());

        return save(image);
    }

    @Override
    public boolean updateImage(MallProductImageUpdateRequest request) {
        // 检查图片是否存在
        MallProductImage existingImage = getById(request.getId());
        if (existingImage == null) {
            throw new ServiceException("商品图片不存在");
        }

        BeanUtils.copyProperties(request, existingImage);

        return updateById(existingImage);
    }

    @Override
    public boolean deleteImage(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("请选择要删除的图片");
        }

        for (Long id : ids) {
            // 检查图片是否存在
            MallProductImage image = getById(id);
            if (image == null) {
                throw new ServiceException("商品图片不存在: " + id);
            }
        }

        return removeByIds(ids);
    }
}




