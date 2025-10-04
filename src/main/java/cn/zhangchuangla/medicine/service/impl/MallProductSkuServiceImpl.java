package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.mapper.MallProductSkuMapper;
import cn.zhangchuangla.medicine.model.entity.MallProductSku;
import cn.zhangchuangla.medicine.model.request.mall.MallProductSkuAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductSkuListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductSkuUpdateRequest;
import cn.zhangchuangla.medicine.service.MallProductSkuService;
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
 * 商城商品SKU规格服务实现类
 * <p>
 * 实现商城商品SKU规格的业务逻辑处理，包括SKU的增删改查、
 * 规格管理、库存配置、价格管理等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:18
 */
@Service
public class MallProductSkuServiceImpl extends ServiceImpl<MallProductSkuMapper, MallProductSku>
        implements MallProductSkuService {

    @Override
    public List<MallProductSku> listSkusByProductId(Long productId) {
        if (productId == null) {
            throw new ServiceException("商品ID不能为空");
        }

        LambdaQueryWrapper<MallProductSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallProductSku::getProductId, productId)
                .orderByAsc(MallProductSku::getSort)
                .orderByDesc(MallProductSku::getCreateTime);

        return list(queryWrapper);
    }

    @Override
    public Page<MallProductSku> listSkusByProductId(MallProductSkuListQueryRequest request) {
        LambdaQueryWrapper<MallProductSku> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getProductId() != null) {
            queryWrapper.eq(MallProductSku::getProductId, request.getProductId());
        }

        if (StringUtils.hasText(request.getSkuName())) {
            queryWrapper.like(MallProductSku::getSkuName, request.getSkuName());
        }

        if (request.getStatus() != null) {
            queryWrapper.eq(MallProductSku::getStatus, request.getStatus());
        }

        if (request.getMinPrice() != null) {
            queryWrapper.ge(MallProductSku::getPrice, request.getMinPrice());
        }

        if (request.getMaxPrice() != null) {
            queryWrapper.le(MallProductSku::getPrice, request.getMaxPrice());
        }

        queryWrapper.orderByAsc(MallProductSku::getSort)
                .orderByDesc(MallProductSku::getCreateTime);

        return page(new Page<>(request.getPageNum(), request.getPageSize()), queryWrapper);
    }

    @Override
    public boolean addSku(MallProductSkuAddRequest request) {
        // 验证规格信息
        if (!StringUtils.hasText(request.getSkuName())) {
            throw new ServiceException("规格名称不能为空");
        }

        // 验证价格
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("SKU价格必须大于0");
        }

        // 验证库存
        if (request.getStock() == null || request.getStock() < 0) {
            throw new ServiceException("SKU库存不能为负数");
        }

        MallProductSku sku = new MallProductSku();
        BeanUtils.copyProperties(request, sku);
        sku.setCreateTime(new Date());
        sku.setUpdateTime(new Date());

        return save(sku);
    }

    @Override
    public boolean updateSku(MallProductSkuUpdateRequest request) {
        // 检查SKU是否存在
        MallProductSku existingSku = getById(request.getId());
        if (existingSku == null) {
            throw new ServiceException("商品SKU不存在");
        }

        // 验证规格信息
        if (!StringUtils.hasText(request.getSkuName())) {
            throw new ServiceException("规格名称不能为空");
        }

        // 验证价格
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("SKU价格必须大于0");
        }

        // 验证库存
        if (request.getStock() == null || request.getStock() < 0) {
            throw new ServiceException("SKU库存不能为负数");
        }

        BeanUtils.copyProperties(request, existingSku);
        existingSku.setUpdateTime(new Date());

        return updateById(existingSku);
    }

    @Override
    public boolean deleteSku(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("请选择要删除的SKU");
        }

        for (Long id : ids) {
            // 检查SKU是否存在
            MallProductSku sku = getById(id);
            if (sku == null) {
                throw new ServiceException("商品SKU不存在: " + id);
            }
        }

        return removeByIds(ids);
    }
}




