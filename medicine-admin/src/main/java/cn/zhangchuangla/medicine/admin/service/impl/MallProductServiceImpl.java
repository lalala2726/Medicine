package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallOrderItemMapper;
import cn.zhangchuangla.medicine.admin.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.admin.model.dto.ProductSalesDto;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.admin.task.MallProductSearchIndexer;
import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.AgentDrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.entity.DrugDetail;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.enums.DeliveryTypeEnum;
import cn.zhangchuangla.medicine.model.enums.OrderStatusEnum;
import cn.zhangchuangla.medicine.model.request.MallProductAddRequest;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.request.MallProductUpdateRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        implements MallProductService, BaseService {

    /**
     * 商品索引批处理的默认批次大小。
     */
    private static final int DEFAULT_INDEX_BATCH_SIZE = 500;

    private final MallProductMapper mallProductMapper;
    private final MallCategoryService mallCategoryService;
    private final MallProductImageService mallProductImageService;
    private final MallMedicineDetailService medicineDetailService;
    private final MallProductStatsService mallProductStatsService;
    private final MallProductTagService mallProductTagService;
    private final MallProductTagRelService mallProductTagRelService;
    private final MallOrderItemMapper mallOrderItemMapper;
    private final MallProductSearchIndexer mallProductSearchIndexer;
    private final RedisCache redisCache;

    @Override
    public Page<MallProduct> listMallProduct(MallProductListQueryRequest request) {
        mallProductTagService.fillTagFilterGroups(request);
        Page<MallProduct> page = page(new Page<>(request.getPageNum(), request.getPageSize()));
        return mallProductMapper.listMallProduct(page, request);
    }

    @Override
    public Page<MallProductDetailDto> listMallProductWithCategory(MallProductListQueryRequest request) {
        mallProductTagService.fillTagFilterGroups(request);
        // 先查询商品列表
        Page<MallProductDetailDto> page = mallProductMapper.listMallProductWithCategory(request.toPage(), request);

        if (page.getRecords().isEmpty()) {
            return page;
        }

        // 查询商品的销量
        List<ProductSalesDto> productSales = mallProductStatsService.getProductSales();

        // 将销量数据转换为Map，方便快速查找
        HashMap<Long, Integer> salesMap = new HashMap<>();
        if (productSales != null && !productSales.isEmpty()) {
            productSales.forEach(sales -> salesMap.put(sales.getProductId(), sales.getSales()));
        }

        List<Long> productIds = page.getRecords().stream().map(MallProduct::getId).toList();
        Map<Long, String> coverImageMap = mallProductImageService.getFirstImageByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(MallProductImage::getProductId, MallProductImage::getImageUrl));
        Map<Long, List<cn.zhangchuangla.medicine.model.vo.MallProductTagVo>> tagMap =
                mallProductTagService.listTagVoMapByProductIds(productIds);

        // 为每个商品设置销量
        page.getRecords().forEach(product -> {
            Long productId = product.getId();
            Integer sales = salesMap.get(productId);
            product.setSales(sales != null ? sales : 0);
            String cover = coverImageMap.get(productId);
            product.setImages(cover == null ? List.of() : List.of(cover));
            product.setTags(tagMap.getOrDefault(productId, List.of()));
        });
        return page;
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
        List<String> images = mallProductImageService.lambdaQuery()
                .eq(MallProductImage::getProductId, id)
                .orderByAsc(MallProductImage::getSort)
                .list()
                .stream()
                .map(MallProductImage::getImageUrl)
                .toList();
        product.setImages(images);
        product.setTags(mallProductTagService.listTagVoMapByProductIds(List.of(id)).getOrDefault(id, List.of()));
        return product;
    }

    @Override
    public List<MallProductDetailDto> getMallProductByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        // 批量查询商品基础信息
        List<MallProductDetailDto> products = mallProductMapper.getMallProductDetailByIds(ids);
        if (products.isEmpty()) {
            return List.of();
        }

        // 批量查询图片并按 productId 分组
        List<Long> productIds = products.stream().map(MallProduct::getId).toList();
        Map<Long, List<String>> imageMap = mallProductImageService.lambdaQuery()
                .in(MallProductImage::getProductId, productIds)
                .orderByAsc(MallProductImage::getSort)
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                        MallProductImage::getProductId,
                        Collectors.mapping(MallProductImage::getImageUrl, Collectors.toList())
                ));
        Map<Long, List<cn.zhangchuangla.medicine.model.vo.MallProductTagVo>> tagMap =
                mallProductTagService.listTagVoMapByProductIds(productIds);

        // 设置图片到每个商品
        products.forEach(product -> {
            List<String> images = imageMap.getOrDefault(product.getId(), List.of());
            product.setImages(images);
            product.setTags(tagMap.getOrDefault(product.getId(), List.of()));
        });

        return products;
    }

    @Override
    public List<AgentDrugDetailDto> getDrugDetailByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        // 批量查询商品信息（获取商品名称）
        List<MallProduct> products = listByIds(productIds);
        if (products.isEmpty()) {
            return List.of();
        }
        Map<Long, String> productNameMap = products.stream()
                .collect(Collectors.toMap(MallProduct::getId, MallProduct::getName, (existing, ignore) -> existing));

        // 批量查询药品详情
        List<DrugDetail> drugDetails = medicineDetailService.lambdaQuery()
                .in(DrugDetail::getProductId, productIds)
                .list();
        if (drugDetails.isEmpty()) {
            return List.of();
        }

        // 组装结果
        return drugDetails.stream()
                .map(drug -> {
                    DrugDetailDto drugDetailDto = copyProperties(drug, DrugDetailDto.class);
                    AgentDrugDetailDto dto = new AgentDrugDetailDto();
                    dto.setProductId(drug.getProductId());
                    dto.setProductName(productNameMap.get(drug.getProductId()));
                    dto.setDrugDetail(drugDetailDto);
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = RedisConstants.MallProduct.CACHE_NAME, allEntries = true)
    public boolean addMallProduct(MallProductAddRequest request) {
        List<Long> normalizedTagIds = mallProductTagService.normalizeEnabledTagIds(request.getTagIds());

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
        product.setCreateTime(new Date());
        product.setCreateBy(SecurityUtils.getUsername());

        boolean save = save(product);

        // 仅聚焦商品与图片：确保至少一张图片后批量写入图片表
        Assert.isTrue(request.getImages() != null && !request.getImages().isEmpty(), "商品图片至少需要上传一张图片");
        mallProductImageService.addProductImages(request.getImages(), product.getId());
        mallProductTagRelService.replaceProductTags(product.getId(), normalizedTagIds);
        // 上述是通用的商城属性,下面是药品特有的属性
        DrugDetail drugDetail = copyProperties(request.getDrugDetail(), DrugDetail.class);
        drugDetail.setProductId(product.getId());
        boolean result = medicineDetailService.addMedicineDetail(drugDetail);

        boolean success = save && result;
        if (success) {
            runAfterCommit(() -> mallProductSearchIndexer.reindexAsync(product.getId()));
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = RedisConstants.MallProduct.CACHE_NAME, key = "#request.id", condition = "#request.id != null")
    public boolean updateMallProduct(MallProductUpdateRequest request) {
        List<Long> normalizedTagIds = mallProductTagService.normalizeEnabledTagIds(request.getTagIds());

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
        mallProductTagRelService.replaceProductTags(existingProduct.getId(), normalizedTagIds);

        // 更新药品详情
        if (request.getDrugDetail() != null) {
            DrugDetail drugDetail = copyProperties(request.getDrugDetail(), DrugDetail.class);
            drugDetail.setProductId(existingProduct.getId());
            medicineDetailService.updateMedicineDetail(drugDetail);
        }

        boolean updated = updateById(existingProduct);
        if (updated) {
            runAfterCommit(() -> mallProductSearchIndexer.reindexAsync(existingProduct.getId()));
        }
        return updated;
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

        List<String> limitedOrderStatuses = List.of(
                OrderStatusEnum.PENDING_SHIPMENT.getType(),
                OrderStatusEnum.PENDING_RECEIPT.getType(),
                OrderStatusEnum.AFTER_SALE.getType()
        );
        List<Long> blockedProductIds = mallOrderItemMapper.findProductIdsWithOrderStatuses(ids, limitedOrderStatuses);
        if (!blockedProductIds.isEmpty()) {
            throw new ServiceException("商品存在待发货/待收货/售后中的订单，无法删除: " + blockedProductIds);
        }

        // 删除关联的图片
        mallProductImageService.removeImagesById(ids);

        // 删除关联的商品标签
        mallProductTagRelService.removeByProductIds(ids);

        // 删除关联的药品详情
        medicineDetailService.deleteMedicineDetailByProductIds(ids);

        boolean removed = removeByIds(ids);
        if (removed) {
            runAfterCommit(() -> mallProductSearchIndexer.removeAsync(ids));
        }
        return removed;
    }

    @Override
    public void reindexOnShelfBatch() {
        Long cursor = redisCache.getCacheObject(RedisConstants.MallProductIndex.INDEX_CURSOR_KEY);
        if (cursor == null || cursor < 0) {
            cursor = 0L;
        }

        // 分批读取上架商品，避免一次性拉取全量数据
        List<MallProductDetailDto> batch = mallProductMapper.listOnShelfForIndex(cursor, DEFAULT_INDEX_BATCH_SIZE);
        if (batch.isEmpty()) {
            // 没有更多数据时重置游标，方便下次重新全量同步
            redisCache.setCacheObject(RedisConstants.MallProductIndex.INDEX_CURSOR_KEY, 0L);
            return;
        }

        List<Long> productIds = batch.stream().map(MallProduct::getId).toList();
        Map<Long, String> coverImageMap = mallProductImageService.getFirstImageByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(MallProductImage::getProductId, MallProductImage::getImageUrl));

        batch.forEach(product -> {
            String cover = coverImageMap.get(product.getId());
            product.setImages(cover == null ? List.of() : List.of(cover));
        });
        Map<Long, List<cn.zhangchuangla.medicine.model.vo.MallProductTagVo>> tagMap =
                mallProductTagService.listTagVoMapByProductIds(productIds);
        batch.forEach(product -> product.setTags(tagMap.getOrDefault(product.getId(), List.of())));

        // 只发布 MQ 消息，索引工作由消费者完成
        mallProductSearchIndexer.reindexBatch(batch);

        Long nextCursor = batch.getLast().getId();
        redisCache.setCacheObject(RedisConstants.MallProductIndex.INDEX_CURSOR_KEY, nextCursor);
    }

    /**
     * 在事务提交后触发异步同步，避免读取到未提交数据。
     */
    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
