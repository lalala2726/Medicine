package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.admin.service.MallCategoryService;
import cn.zhangchuangla.medicine.admin.service.MallProductImageService;
import cn.zhangchuangla.medicine.admin.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDto;
import cn.zhangchuangla.medicine.model.entity.MallCategory;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.enums.DeliveryTypeEnum;
import cn.zhangchuangla.medicine.model.request.mall.product.MallProductAddRequest;
import cn.zhangchuangla.medicine.model.request.mall.product.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.request.mall.product.MallProductUpdateRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * created on 2025/10/4
 */
@Service
@RequiredArgsConstructor
public class MallProductServiceImpl extends ServiceImpl<MallProductMapper, MallProduct>
        implements MallProductService {

    private final MallProductMapper mallProductMapper;
    private final MallCategoryService mallCategoryService;
    private final MallProductImageService mallProductImageService;

    @Override
    public Page<MallProduct> listMallProduct(MallProductListQueryRequest request) {
        Page<MallProduct> page = page(new Page<>(request.getPageNum(), request.getPageSize()));
        return mallProductMapper.listMallProduct(page, request);
    }

    @Override
    public Page<MallProductDto> listMallProductWithCategory(MallProductListQueryRequest request) {
        Page<MallProductDto> page = request.toPage();
        return mallProductMapper.listMallProductWithCategory(page, request);
    }

    @Override
    public MallProductDetailDto getMallProductById(Long id) {
        if (id == null) {
            throw new ServiceException("商品ID不能为空");
        }

        MallProductDetailDto product = mallProductMapper.getMallProductDetailById(id);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        if (product.getCategoryId() != null) {
            MallCategory category = mallCategoryService.getCategoryById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getName());
            }
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // 后台表单中按约定首图为列表缩略图
            product.setImage(product.getImages().get(0));
        }

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = RedisConstants.MallProduct.CACHE_NAME, allEntries = true)
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

        // 检查商品分类是否存在
        boolean isExist = mallCategoryService.isProductCategoryExist(request.getCategoryId());
        if (!isExist) {
            throw new ServiceException("商品分类不存在");
        }

        // 检查配送方式是否存在
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromLegacyCode(request.getDeliveryType());
        Assert.isTrue(deliveryTypeEnum != null, "配送方式不存在");

        MallProduct product = new MallProduct();
        BeanUtils.copyProperties(request, product);
        product.setSalesVolume(0L); // 初始销量为0
        product.setCreateTime(new Date());
        product.setCreateBy(SecurityUtils.getUsername());

        boolean save = save(product);

        // 仅聚焦商品与图片：确保至少一张图片后批量写入图片表
        Assert.isTrue(request.getImages() != null && !request.getImages().isEmpty(), "商品图片至少需要上传一张图片");
        mallProductImageService.addProductImages(request.getImages(), product.getId());
        // 上述是通用的商城属性,下面是药品特有的属性

        // todo 添加药品的属性
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = RedisConstants.MallProduct.CACHE_NAME, key = "#request.id", condition = "#request.id != null")
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

        // 检查配送方式是否存在
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromLegacyCode(request.getDeliveryType());
        Assert.isTrue(deliveryTypeEnum != null, "配送方式不存在");

        BeanUtils.copyProperties(request, existingProduct);
        existingProduct.setUpdateTime(new Date());
        existingProduct.setUpdateBy(SecurityUtils.getUsername());
        // 更新商品主图集合，同样保障后台始终有可展示的图片
        Assert.isTrue(request.getImages() != null && !request.getImages().isEmpty(), "商品图片至少需要上传一张图片");
        mallProductImageService.updateProductImageById(request.getImages(), existingProduct.getId());

        return updateById(existingProduct);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = RedisConstants.MallProduct.CACHE_NAME, allEntries = true)
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

        // 删除关联的图片
        mallProductImageService.removeImagesById(ids);
        return removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreStock(Long productId, Integer quantity) {
        Assert.isPositive(quantity, "商品数量不能小于0");
        Assert.isPositive(productId, "商品ID不能小于0");

        MallProduct mallProduct = lambdaQuery()
                .eq(MallProduct::getId, productId)
                .select(MallProduct::getStock, MallProduct::getVersion)
                .one();

        if (mallProduct == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品不存在");
        }

        Integer currentStock = mallProduct.getStock();
        int newStock = (currentStock == null ? 0 : currentStock) + quantity;
        int currentVersion = mallProduct.getVersion() == null ? 0 : mallProduct.getVersion();

        boolean updated = lambdaUpdate()
                .eq(MallProduct::getId, productId)
                .eq(MallProduct::getVersion, currentVersion)
                .set(MallProduct::getStock, newStock)
                .set(MallProduct::getVersion, currentVersion + 1)
                .update();

        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "库存更新失败，请重试");
        }
    }

}
