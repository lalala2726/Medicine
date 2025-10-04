package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.mapper.MallProductShippingMapper;
import cn.zhangchuangla.medicine.model.entity.MallProductShipping;
import cn.zhangchuangla.medicine.model.request.mall.MallProductShippingAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductShippingListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductShippingUpdateRequest;
import cn.zhangchuangla.medicine.service.MallProductShippingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商城商品运费配置服务实现类
 * <p>
 * 实现商城商品运费模板的业务逻辑处理，包括运费模板的增删改查、
 * 运费类型管理、价格配置等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:15
 */
@Service
public class MallProductShippingServiceImpl extends ServiceImpl<MallProductShippingMapper, MallProductShipping>
        implements MallProductShippingService {

    @Override
    public Page<MallProductShipping> listShippingsByProductId(MallProductShippingListQueryRequest request) {
        LambdaQueryWrapper<MallProductShipping> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getName())) {
            queryWrapper.like(MallProductShipping::getName, request.getName());
        }

        if (StringUtils.hasText(request.getType())) {
            queryWrapper.eq(MallProductShipping::getType, request.getType());
        }

        if (request.getPrice() != null) {
            queryWrapper.eq(MallProductShipping::getPrice, request.getPrice());
        }

        queryWrapper.orderByDesc(MallProductShipping::getCreateTime);

        return page(new Page<>(request.getPageNum(), request.getPageSize()), queryWrapper);
    }

    @Override
    public boolean addShipping(MallProductShippingAddRequest request) {
        // 验证模板名称是否重复
        if (StringUtils.hasText(request.getName())) {
            LambdaQueryWrapper<MallProductShipping> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MallProductShipping::getName, request.getName());
            if (count(queryWrapper) > 0) {
                throw new ServiceException("运费模板名称已存在");
            }
        }

        // 验证运费类型
        if (!"free".equals(request.getType()) && !"fixed".equals(request.getType())) {
            throw new ServiceException("运费类型只能是free（包邮）或fixed（固定运费）");
        }

        // 如果是固定运费，必须设置运费价格
        if ("fixed".equals(request.getType()) && (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0)) {
            throw new ServiceException("固定运费必须设置运费价格且价格不能为负数");
        }

        MallProductShipping shipping = new MallProductShipping();
        BeanUtils.copyProperties(request, shipping);
        shipping.setCreateTime(new Date());
        shipping.setUpdateTime(new Date());

        return save(shipping);
    }

    @Override
    public boolean updateShipping(MallProductShippingUpdateRequest request) {
        // 检查运费模板是否存在
        MallProductShipping existingShipping = getById(request.getId());
        if (existingShipping == null) {
            throw new ServiceException("运费模板不存在");
        }

        // 验证模板名称是否重复（排除自己）
        if (StringUtils.hasText(request.getName()) && !existingShipping.getName().equals(request.getName())) {
            LambdaQueryWrapper<MallProductShipping> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MallProductShipping::getName, request.getName())
                    .ne(MallProductShipping::getId, request.getId());
            if (count(queryWrapper) > 0) {
                throw new ServiceException("运费模板名称已存在");
            }
        }

        // 验证运费类型
        if (!"free".equals(request.getType()) && !"fixed".equals(request.getType())) {
            throw new ServiceException("运费类型只能是free（包邮）或fixed（固定运费）");
        }

        // 如果是固定运费，必须设置运费价格
        if ("fixed".equals(request.getType()) && (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0)) {
            throw new ServiceException("固定运费必须设置运费价格且价格不能为负数");
        }

        BeanUtils.copyProperties(request, existingShipping);
        existingShipping.setUpdateTime(new Date());

        return updateById(existingShipping);
    }

    @Override
    public boolean deleteShipping(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("请选择要删除的运费模板");
        }

        for (Long id : ids) {
            // 检查运费模板是否存在
            MallProductShipping shipping = getById(id);
            if (shipping == null) {
                throw new ServiceException("运费模板不存在: " + id);
            }
        }

        return removeByIds(ids);
    }
}




