package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.core.common.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.model.dto.MallProductDto;
import cn.zhangchuangla.medicine.common.core.model.entity.MallProduct;
import cn.zhangchuangla.medicine.common.core.model.entity.MedicineStock;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductAddRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.common.core.model.request.mall.MallProductUpdateRequest;
import cn.zhangchuangla.medicine.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.service.MallCategoryService;
import cn.zhangchuangla.medicine.service.MallProductService;
import cn.zhangchuangla.medicine.service.MedicineStockService;
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
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final MedicineStockService medicineStockService;

    @Override
    public Page<MallProduct> listMallProduct(MallProductListQueryRequest request) {
        Page<MallProduct> page = page(new Page<>(request.getPageNum(), request.getPageSize()));
        Page<MallProduct> mallProductPage = mallProductMapper.listMallProduct(page, request);

        // 使用通用方法绑定库存信息
        bindStockMapping(
                mallProductPage.getRecords(),
                MallProduct::getBindType,
                MallProduct::getMedicineStockId,
                MallProduct::setStock
        );

        return mallProductPage;
    }

    @Override
    public Page<MallProductDto> listMallProductWithCategory(MallProductListQueryRequest request) {
        Page<MallProductDto> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<MallProductDto> mallProductDtoPage = mallProductMapper.listMallProductWithCategory(page, request);

        // 使用通用方法绑定库存信息
        bindStockMapping(
                mallProductDtoPage.getRecords(),
                MallProductDto::getBindType,
                MallProductDto::getMedicineStockId,
                MallProductDto::setStock
        );

        return mallProductDtoPage;
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

        // 批量检查商品是否存在，避免循环查询数据库
        List<MallProduct> products = listByIds(ids);
        if (products.size() != ids.size()) {
            // 找出不存在的商品ID
            List<Long> existIds = products.stream()
                    .map(MallProduct::getId)
                    .toList();
            List<Long> notExistIds = ids.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();
            throw new ServiceException("商品不存在: " + notExistIds);
        }

        return removeByIds(ids);
    }

    /**
     * 绑定库存映射的通用方法
     *
     * @param products              商品列表（支持MallProduct或MallProductDto）
     * @param bindTypeGetter        用于获取绑定类型的函数
     * @param medicineStockIdGetter 用于获取库存ID的函数
     * @param stockSetter           用于设置库存数量的函数
     * @param <T>                   商品类型
     */
    private <T> void bindStockMapping(List<T> products,
                                      Function<T, Integer> bindTypeGetter,
                                      Function<T, Long> medicineStockIdGetter,
                                      BiConsumer<T, Integer> stockSetter) {
        // 筛选出需要查询库存的商品ID（绑定库存的商品）
        List<Long> medicineStockIds = products.stream()
                .filter(product -> bindTypeGetter.apply(product) == 1 && medicineStockIdGetter.apply(product) != null)
                .map(medicineStockIdGetter)
                .toList();

        if (!medicineStockIds.isEmpty()) {
            // 统一查询库存信息
            LambdaQueryWrapper<MedicineStock> queryWrapper = new LambdaQueryWrapper<MedicineStock>()
                    .in(MedicineStock::getId, medicineStockIds);
            List<MedicineStock> medicineStocks = medicineStockService.list(queryWrapper);

            // 创建库存ID到库存数量的映射，避免嵌套循环
            Map<Long, Integer> stockMap = medicineStocks.stream()
                    .collect(Collectors.toMap(MedicineStock::getId, MedicineStock::getQuantity));

            // 为商品设置库存信息
            products.stream()
                    .filter(product -> bindTypeGetter.apply(product) == 1 && medicineStockIdGetter.apply(product) != null)
                    .forEach(product -> {
                        Long stockId = medicineStockIdGetter.apply(product);
                        Integer stock = stockMap.get(stockId);
                        if (stock != null) {
                            stockSetter.accept(product, stock);
                        }
                    });
        }
    }

    /**
     * 验证药品绑定信息的通用方法
     *
     * @param medicineId      药品ID
     * @param medicineStockId 药品库存ID
     * @param isUpdate        是否为更新操作
     */
    private void validateMedicineInfo(Long medicineId, Long medicineStockId, boolean isUpdate) {
        if (medicineId == null && medicineStockId == null) {
            String message = isUpdate ? "绑定库存模式下，药品ID和药品库存ID至少需要提供一个"
                    : "药品ID和药品库存ID至少需要提供一个";
            throw new ServiceException(message);
        }

        if (medicineId != null) {
            if (medicineId <= 0) {
                throw new ServiceException("药品ID必须大于0");
            }
            boolean medicineExists = mallCategoryService.isMedicineExist(medicineId);
            if (!medicineExists) {
                throw new ServiceException("药品不存在，ID: " + medicineId);
            }
        }

        if (medicineStockId != null) {
            if (medicineStockId <= 0) {
                throw new ServiceException("药品库存ID必须大于0");
            }
            boolean stockExists = mallCategoryService.isMedicineStockExist(medicineStockId);
            if (!stockExists) {
                throw new ServiceException("药品库存不存在，ID: " + medicineStockId);
            }
        }

        if (medicineId != null && medicineStockId != null && !medicineId.equals(medicineStockId)) {
            throw new ServiceException(String.format("药品ID(%d)与药品库存ID(%d)不匹配", medicineId, medicineStockId));
        }
    }

    // 修改原方法调用
    private void validateMedicineBinding(MallProductAddRequest request) {
        validateMedicineInfo(request.getMedicineId(), request.getMedicineStockId(), false);
    }

    private void validateMedicineBindingForUpdate(MallProductUpdateRequest request) {
        validateMedicineInfo(request.getMedicineId(), request.getMedicineStockId(), true);
    }
}




