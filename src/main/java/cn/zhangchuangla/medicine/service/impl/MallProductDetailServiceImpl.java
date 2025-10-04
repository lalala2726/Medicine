package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.mapper.MallProductDetailMapper;
import cn.zhangchuangla.medicine.model.entity.MallProductDetail;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductDetailUpdateRequest;
import cn.zhangchuangla.medicine.service.MallProductDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 商城商品详情服务实现类
 * <p>
 * 实现商城商品详情的业务逻辑处理，包括详情的增删改查、
 * 详情内容管理、商品关联等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:37
 */
@Service
public class MallProductDetailServiceImpl extends ServiceImpl<MallProductDetailMapper, MallProductDetail>
        implements MallProductDetailService {

    @Override
    public MallProductDetail getDetailByProductId(Long productId) {
        if (productId == null) {
            throw new ServiceException("商品ID不能为空");
        }

        LambdaQueryWrapper<MallProductDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallProductDetail::getProductId, productId);
        return getOne(queryWrapper);
    }

    @Override
    public Page<MallProductDetail> listDetailsByProductId(MallProductDetailListQueryRequest request) {
        LambdaQueryWrapper<MallProductDetail> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getProductId() != null) {
            queryWrapper.eq(MallProductDetail::getProductId, request.getProductId());
        }

        queryWrapper.orderByDesc(MallProductDetail::getCreateTime);

        return page(new Page<>(request.getPageNum(), request.getPageSize()), queryWrapper);
    }

    @Override
    public boolean addDetail(MallProductDetailAddRequest request) {
        // 检查该商品是否已有详情
        LambdaQueryWrapper<MallProductDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallProductDetail::getProductId, request.getProductId());
        if (count(queryWrapper) > 0) {
            throw new ServiceException("该商品已存在详情");
        }

        MallProductDetail detail = new MallProductDetail();
        BeanUtils.copyProperties(request, detail);
        detail.setCreateTime(new Date());
        detail.setUpdateTime(new Date());

        return save(detail);
    }

    @Override
    public boolean updateDetail(MallProductDetailUpdateRequest request) {
        // 检查详情是否存在
        MallProductDetail existingDetail = getById(request.getId());
        if (existingDetail == null) {
            throw new ServiceException("商品详情不存在");
        }

        // 如果修改了商品ID，检查新商品是否已有详情
        if (!existingDetail.getProductId().equals(request.getProductId())) {
            LambdaQueryWrapper<MallProductDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MallProductDetail::getProductId, request.getProductId())
                    .ne(MallProductDetail::getId, request.getId());
            if (count(queryWrapper) > 0) {
                throw new ServiceException("该商品已存在详情");
            }
        }

        BeanUtils.copyProperties(request, existingDetail);
        existingDetail.setUpdateTime(new Date());

        return updateById(existingDetail);
    }

    @Override
    public boolean deleteDetail(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("请选择要删除的商品详情");
        }

        for (Long id : ids) {
            // 检查详情是否存在
            MallProductDetail detail = getById(id);
            if (detail == null) {
                throw new ServiceException("商品详情不存在: " + id);
            }
        }

        return removeByIds(ids);
    }
}




