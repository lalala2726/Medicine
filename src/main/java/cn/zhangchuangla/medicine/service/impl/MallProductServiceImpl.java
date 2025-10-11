package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.model.dto.MallProductDto;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.request.mall.MallProductAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.MallProductUpdateRequest;
import cn.zhangchuangla.medicine.service.MallCategoryService;
import cn.zhangchuangla.medicine.service.MallProductService;
import cn.zhangchuangla.medicine.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MallProductServiceImpl extends ServiceImpl<MallProductMapper, MallProduct>
        implements MallProductService {

    private final MallProductMapper mallProductMapper;
    private final MallCategoryService mallCategoryService;

    @Override
    public Page<MallProduct> listMallProduct(MallProductListQueryRequest request) {
        Page<MallProduct> page = page(new Page<>(request.getPageNum(), request.getPageSize()));
        return mallProductMapper.listMallProduct(page, request);
    }

    @Override
    public Page<MallProductDto> listMallProductWithCategory(MallProductListQueryRequest request) {
        Page<MallProductDto> page = new Page<>(request.getPageNum(), request.getPageSize());
        return mallProductMapper.listMallProductWithCategory(page, request);
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

        //检查商品分类是否存在
        boolean isExist = mallCategoryService.isProductCategoryExist(request.getCategoryId());
        if (!isExist) {
            throw new ServiceException("商品分类不存在");
        }

        // 如果是绑定库存，验证药品相关信息
        if (request.getBindType() == 1) {
            validateMedicineBinding(request);
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

        // 如果是绑定库存，验证药品相关信息
        if (request.getBindType() != null && request.getBindType() == 1) {
            validateMedicineBindingForUpdate(request);
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

    /**
     * 验证药品绑定信息
     *
     * @param request 商品添加请求
     */
    private void validateMedicineBinding(MallProductAddRequest request) {
        Long medicineId = request.getMedicineId();
        Long medicineStockId = request.getMedicineStockId();

        // 验证药品ID和药品库存ID不能为空
        if (medicineId == null && medicineStockId == null) {
            throw new ServiceException("药品ID和药品库存ID至少需要提供一个");
        }

        // 如果提供了药品ID，验证其有效性
        if (medicineId != null) {
            if (medicineId <= 0) {
                throw new ServiceException("药品ID必须大于0");
            }
            boolean medicineExists = mallCategoryService.isMedicineExist(medicineId);
            if (!medicineExists) {
                throw new ServiceException("药品不存在，ID: " + medicineId);
            }
        }

        // 如果提供了药品库存ID，验证其有效性
        if (medicineStockId != null) {
            if (medicineStockId <= 0) {
                throw new ServiceException("药品库存ID必须大于0");
            }
            boolean stockExists = mallCategoryService.isMedicineStockExist(medicineStockId);
            if (!stockExists) {
                throw new ServiceException("药品库存不存在，ID: " + medicineStockId);
            }
        }

        // 如果同时提供了药品ID和药品库存ID，验证它们是否匹配
        if (medicineId != null && medicineStockId != null) {
            if (!medicineId.equals(medicineStockId)) {
                throw new ServiceException(String.format("药品ID(%d)与药品库存ID(%d)不匹配", medicineId, medicineStockId));
            }
        }
    }

    /**
     * 验证药品绑定信息（更新版本）
     *
     * @param request 商品更新请求
     */
    private void validateMedicineBindingForUpdate(MallProductUpdateRequest request) {
        Long medicineId = request.getMedicineId();
        Long medicineStockId = request.getMedicineStockId();

        // 验证药品ID和药品库存ID不能为空（如果设置了绑定类型为1）
        if (medicineId == null && medicineStockId == null) {
            throw new ServiceException("绑定库存模式下，药品ID和药品库存ID至少需要提供一个");
        }

        // 如果提供了药品ID，验证其有效性
        if (medicineId != null) {
            if (medicineId <= 0) {
                throw new ServiceException("药品ID必须大于0");
            }
            boolean medicineExists = mallCategoryService.isMedicineExist(medicineId);
            if (!medicineExists) {
                throw new ServiceException("药品不存在，ID: " + medicineId);
            }
        }

        // 如果提供了药品库存ID，验证其有效性
        if (medicineStockId != null) {
            if (medicineStockId <= 0) {
                throw new ServiceException("药品库存ID必须大于0");
            }
            boolean stockExists = mallCategoryService.isMedicineStockExist(medicineStockId);
            if (!stockExists) {
                throw new ServiceException("药品库存不存在，ID: " + medicineStockId);
            }
        }

        // 如果同时提供了药品ID和药品库存ID，验证它们是否匹配
        if (medicineId != null && medicineStockId != null) {
            if (!medicineId.equals(medicineStockId)) {
                throw new ServiceException(String.format("药品ID(%d)与药品库存ID(%d)不匹配", medicineId, medicineStockId));
            }
        }
    }
}




