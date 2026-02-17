package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.mapper.MallMedicineDetailMapper;
import cn.zhangchuangla.medicine.agent.mapper.MallOrderItemMapper;
import cn.zhangchuangla.medicine.agent.mapper.MallProductImageMapper;
import cn.zhangchuangla.medicine.agent.mapper.MallProductMapper;
import cn.zhangchuangla.medicine.agent.model.dto.ProductSalesDto;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.agent.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.entity.DrugDetail;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MallProductServiceImpl implements MallProductService {

    private final MallProductMapper mallProductMapper;
    private final MallProductImageMapper mallProductImageMapper;
    private final MallOrderItemMapper mallOrderItemMapper;
    private final MallMedicineDetailMapper mallMedicineDetailMapper;

    @Override
    public Page<MallProductDetailDto> listProducts(MallProductListQueryRequest request) {
        MallProductListQueryRequest safeRequest = request == null ? new MallProductListQueryRequest() : request;
        Page<MallProductDetailDto> page = mallProductMapper.listMallProductWithCategory(safeRequest.toPage(), safeRequest);
        List<MallProductDetailDto> records = page.getRecords();
        if (records.isEmpty()) {
            return page;
        }

        List<Long> productIds = records.stream()
                .map(MallProduct::getId)
                .filter(Objects::nonNull)
                .toList();
        if (productIds.isEmpty()) {
            return page;
        }

        Map<Long, Integer> salesMap = new HashMap<>();
        List<ProductSalesDto> productSales = mallOrderItemMapper.getProductSalesByIds(productIds);
        if (productSales != null && !productSales.isEmpty()) {
            productSales.forEach(item -> salesMap.put(item.getProductId(), item.getSales()));
        }

        Map<Long, String> coverImageMap = getFirstImageMap(productIds);
        records.forEach(product -> {
            Integer sales = salesMap.get(product.getId());
            product.setSales(sales == null ? 0 : sales);
            String cover = coverImageMap.get(product.getId());
            product.setImages(cover == null ? List.of() : List.of(cover));
        });
        return page;
    }

    @Override
    public List<AdminAgentProductDetailVo> getProductDetail(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        List<MallProductDetailDto> products = mallProductMapper.getMallProductDetailByIds(productIds);
        if (products.isEmpty()) {
            return List.of();
        }

        List<Long> ids = products.stream().map(MallProduct::getId).toList();
        Map<Long, List<String>> imageMap = getImageMap(ids);
        products.forEach(product -> product.setImages(imageMap.getOrDefault(product.getId(), List.of())));
        return BeanCotyUtils.copyListProperties(products, AdminAgentProductDetailVo.class);
    }

    @Override
    public List<AdminAgentDrugDetailVo> getDrugDetail(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        List<MallProduct> products = mallProductMapper.selectByIds(productIds);
        if (products.isEmpty()) {
            return List.of();
        }

        Map<Long, String> productNameMap = products.stream()
                .collect(Collectors.toMap(MallProduct::getId, MallProduct::getName, (left, right) -> left));

        List<DrugDetail> drugDetails = mallMedicineDetailMapper.selectList(new LambdaQueryWrapper<DrugDetail>()
                .in(DrugDetail::getProductId, productIds));
        if (drugDetails.isEmpty()) {
            return List.of();
        }

        return drugDetails.stream()
                .map(drug -> {
                    AdminAgentDrugDetailVo detailVo = new AdminAgentDrugDetailVo();
                    detailVo.setProductId(drug.getProductId());
                    detailVo.setProductName(productNameMap.get(drug.getProductId()));
                    detailVo.setDrugDetail(BeanCotyUtils.copyProperties(drug, DrugDetailDto.class));
                    return detailVo;
                })
                .toList();
    }

    private Map<Long, String> getFirstImageMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<MallProductImage> images = mallProductImageMapper.selectList(new LambdaQueryWrapper<MallProductImage>()
                .in(MallProductImage::getProductId, productIds)
                .orderByAsc(MallProductImage::getSort));
        if (images.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> result = new LinkedHashMap<>();
        for (MallProductImage image : images) {
            result.putIfAbsent(image.getProductId(), image.getImageUrl());
        }
        return result;
    }

    private Map<Long, List<String>> getImageMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<MallProductImage> images = mallProductImageMapper.selectList(new LambdaQueryWrapper<MallProductImage>()
                .in(MallProductImage::getProductId, productIds)
                .orderByAsc(MallProductImage::getSort));
        if (images.isEmpty()) {
            return Map.of();
        }

        return images.stream().collect(Collectors.groupingBy(
                MallProductImage::getProductId,
                Collectors.mapping(MallProductImage::getImageUrl, Collectors.toList())
        ));
    }
}
