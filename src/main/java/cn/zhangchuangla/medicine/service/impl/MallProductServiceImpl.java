package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.MallProductAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductUpdateRequest;
import cn.zhangchuangla.medicine.service.MallProductService;
import cn.zhangchuangla.medicine.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商城商品服务实现类
 * <p>
 * 实现商城商品的业务逻辑处理，包括商品的增删改查、
 * 商品列表查询、商品详情获取等功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:34
 */
@Service
public class MallProductServiceImpl extends ServiceImpl<MallProductMapper, MallProduct>
        implements MallProductService {

    @Override
    public Page<MallProduct> listMallProduct(MallProductListQueryRequest request) {
        LambdaQueryWrapper<MallProduct> queryWrapper = new LambdaQueryWrapper<>();

        // 按商品ID查询
        if (request.getId() != null) {
            queryWrapper.eq(MallProduct::getId, request.getId());
        }

        // 按商品名称模糊查询
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            queryWrapper.like(MallProduct::getName, request.getName().trim());
        }

        // 按商品分类ID查询
        if (request.getCategoryId() != null) {
            queryWrapper.eq(MallProduct::getCategoryId, request.getCategoryId());
        }

        // 按状态查询
        if (request.getStatus() != null) {
            queryWrapper.eq(MallProduct::getStatus, request.getStatus());
        }

        // 按库存绑定类型查询
        if (request.getBindType() != null) {
            queryWrapper.eq(MallProduct::getBindType, request.getBindType());
        }

        // 按关联药品ID查询
        if (request.getMedicineId() != null) {
            queryWrapper.eq(MallProduct::getMedicineId, request.getMedicineId());
        }

        // 按价格区间查询
        if (request.getMinPrice() != null) {
            queryWrapper.ge(MallProduct::getPrice, request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            queryWrapper.le(MallProduct::getPrice, request.getMaxPrice());
        }

        // 按排序值升序排序，销量降序排序
        queryWrapper.orderByAsc(MallProduct::getSort)
                .orderByDesc(MallProduct::getSalesVolume)
                .orderByDesc(MallProduct::getCreateTime);

        return page(new Page<>(request.getPageNum(), request.getPageSize()), queryWrapper);
    }

    @Override
    public MallProduct getMallProductById(Long id) {
        if (id == null) {
            throw new ServiceException("商品ID不能为空");
        }

        MallProduct product = getById(id);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        return product;
    }

    @Override
    public boolean addMallProduct(MallProductAddRequest request) {
        // 检查商品名称是否已存在
        LambdaQueryWrapper<MallProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallProduct::getName, request.getName());
        if (count(queryWrapper) > 0) {
            throw new ServiceException("商品名称已存在");
        }

        // 检查价格是否为负数
        if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("商品价格不能为负数");
        }

        // 检查库存是否为负数
        if (request.getStock() < 0) {
            throw new ServiceException("商品库存不能为负数");
        }

        MallProduct product = new MallProduct();
        BeanUtils.copyProperties(request, product);
        product.setSalesVolume(0L); // 初始销量为0
        product.setCreateTime(new Date());
        product.setCreateBy(SecurityUtils.getUsername());

        return save(product);
    }

    @Override
    public boolean updateMallProduct(MallProductUpdateRequest request) {
        // 检查商品是否存在
        MallProduct existingProduct = getById(request.getId());
        if (existingProduct == null) {
            throw new ServiceException("商品不存在");
        }

        // 检查商品名称是否已存在（排除自己）
        LambdaQueryWrapper<MallProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallProduct::getName, request.getName())
                .ne(MallProduct::getId, request.getId());
        if (count(queryWrapper) > 0) {
            throw new ServiceException("商品名称已存在");
        }

        // 检查价格是否为负数
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("商品价格不能为负数");
        }

        // 检查库存是否为负数
        if (request.getStock() != null && request.getStock() < 0) {
            throw new ServiceException("商品库存不能为负数");
        }

        BeanUtils.copyProperties(request, existingProduct);
        existingProduct.setUpdateTime(new Date());
        existingProduct.setUpdateBy(SecurityUtils.getUsername());

        return updateById(existingProduct);
    }

    @Override
    public boolean deleteMallProduct(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("请选择要删除的商品");
        }

        for (Long id : ids) {
            // 检查商品是否存在
            MallProduct product = getById(id);
            if (product == null) {
                throw new ServiceException("商品不存在: " + id);
            }
        }

        return removeByIds(ids);
    }
}




