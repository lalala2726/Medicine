package cn.zhangchuangla.medicine.common.elasticsearch.support;

import cn.zhangchuangla.medicine.common.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * MallProduct -> Elasticsearch 文档的转换器。
 */
public final class MallProductDocumentConverter {

    private MallProductDocumentConverter() {
    }

    /**
     * 构造商品索引文档。
     *
     * @param product 商品详情（包含药品信息、图片）
     * @return Elasticsearch 文档
     */
    public static MallProductDocument from(MallProductDetailDto product) {
        if (product == null) {
            return null;
        }

        DrugDetailDto drugDetail = product.getDrugDetail();
        return MallProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryId(product.getCategoryId())
                .categoryName(product.getCategoryName())
                .price(product.getPrice() == null ? null : product.getPrice().doubleValue())
                .status(product.getStatus())
                .brand(drugDetail == null ? null : drugDetail.getBrand())
                .commonName(drugDetail == null ? null : drugDetail.getCommonName())
                .efficacy(drugDetail == null ? null : drugDetail.getEfficacy())
                .composition(drugDetail == null ? null : drugDetail.getComposition())
                .usageMethod(drugDetail == null ? null : drugDetail.getUsageMethod())
                .warmTips(drugDetail == null ? null : drugDetail.getWarmTips())
                .instruction(drugDetail == null ? null : drugDetail.getInstruction())
                .coverImage(firstImage(product.getImages()))
                .imageUrls(product.getImages())
                .build();
    }

    private static String firstImage(List<String> images) {
        if (CollectionUtils.isEmpty(images)) {
            return null;
        }
        return images.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
