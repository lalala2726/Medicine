package cn.zhangchuangla.medicine.client.rpc;

import cn.zhangchuangla.medicine.client.model.request.MallProductSearchRequest;
import cn.zhangchuangla.medicine.client.model.vo.MallProductSearchVo;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSearchDto;
import cn.zhangchuangla.medicine.model.dto.ClientAgentProductSpecDto;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.request.ClientAgentProductSearchRequest;
import cn.zhangchuangla.medicine.rpc.client.ClientAgentProductRpcService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户端智能体商品 RPC Provider。
 */
@DubboService(interfaceClass = ClientAgentProductRpcService.class, group = "medicine-client", version = "1.0.0")
@RequiredArgsConstructor
public class ClientAgentProductRpcServiceImpl implements ClientAgentProductRpcService {

    /**
     * 客户端智能体商品搜索单页最大返回条数。
     */
    private static final int MAX_PAGE_SIZE = 20;

    /**
     * 商品上架状态。
     */
    private static final int PRODUCT_STATUS_ON_SALE = 1;

    /**
     * 客户端商品服务。
     */
    private final MallProductService mallProductService;

    /**
     * 搜索商品并映射为智能体使用的精简结果。
     *
     * @param request 商品搜索参数
     * @return 搜索结果分页
     */
    @Override
    public PageResult<ClientAgentProductSearchDto> searchProducts(ClientAgentProductSearchRequest request) {
        ClientAgentProductSearchRequest safeRequest = request == null ? new ClientAgentProductSearchRequest() : request;

        MallProductSearchRequest query = new MallProductSearchRequest();
        query.setKeyword(normalizeText(safeRequest.getKeyword()));
        query.setCategoryName(normalizeText(safeRequest.getCategoryName()));
        query.setEfficacy(normalizeText(safeRequest.getUsage()));
        query.setPageNum(Math.max(safeRequest.getPageNum(), 1));
        query.setPageSize(Math.max(safeRequest.getPageSize(), 1));

        PageResult<MallProductSearchVo> result = mallProductService.search(query);
        if (result == null) {
            return new PageResult<>((long) query.getPageNum(), (long) query.getPageSize(), 0L, List.of());
        }
        List<ClientAgentProductSearchDto> rows = result.getRows() == null ? List.of() : result.getRows().stream()
                .map(this::toSearchDto)
                .toList();

        return new PageResult<>(result.getPageNum(), result.getPageSize(), result.getTotal(), rows);
    }

    /**
     * 查询商品详情及其药品说明信息。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @Override
    public MallProductDetailDto getProductDetail(Long productId) {
        return requireVisibleProduct(productId);
    }

    /**
     * 查询商品规格属性。
     *
     * @param productId 商品ID
     * @return 商品规格属性
     */
    @Override
    public ClientAgentProductSpecDto getProductSpec(Long productId) {
        MallProductDetailDto detail = requireVisibleProduct(productId);
        DrugDetailDto drugDetail = detail.getDrugDetail();
        return ClientAgentProductSpecDto.builder()
                .productId(detail.getId())
                .productName(detail.getName())
                .categoryName(detail.getCategoryName())
                .unit(detail.getUnit())
                .commonName(drugDetail == null ? null : drugDetail.getCommonName())
                .composition(drugDetail == null ? null : drugDetail.getComposition())
                .characteristics(drugDetail == null ? null : drugDetail.getCharacteristics())
                .packaging(drugDetail == null ? null : drugDetail.getPackaging())
                .validityPeriod(drugDetail == null ? null : drugDetail.getValidityPeriod())
                .storageConditions(drugDetail == null ? null : drugDetail.getStorageConditions())
                .productionUnit(drugDetail == null ? null : drugDetail.getProductionUnit())
                .approvalNumber(drugDetail == null ? null : drugDetail.getApprovalNumber())
                .executiveStandard(drugDetail == null ? null : drugDetail.getExecutiveStandard())
                .originType(drugDetail == null ? null : drugDetail.getOriginType())
                .brand(drugDetail == null ? null : drugDetail.getBrand())
                .prescription(drugDetail == null ? null : drugDetail.getPrescription())
                .efficacy(drugDetail == null ? null : drugDetail.getEfficacy())
                .usageMethod(drugDetail == null ? null : drugDetail.getUsageMethod())
                .adverseReactions(drugDetail == null ? null : drugDetail.getAdverseReactions())
                .precautions(drugDetail == null ? null : drugDetail.getPrecautions())
                .taboo(drugDetail == null ? null : drugDetail.getTaboo())
                .instruction(drugDetail == null ? null : drugDetail.getInstruction())
                .build();
    }

    /**
     * 将客户端商品搜索结果映射为 RPC DTO。
     *
     * @param source 客户端商品搜索结果
     * @return RPC DTO
     */
    private ClientAgentProductSearchDto toSearchDto(MallProductSearchVo source) {
        if (source == null) {
            return null;
        }
        return ClientAgentProductSearchDto.builder()
                .productId(source.getProductId())
                .productName(source.getProductName())
                .cover(source.getCover())
                .price(source.getPrice())
                .build();
    }

    /**
     * 查询并校验商品是否对客户端智能体可见。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    private MallProductDetailDto requireVisibleProduct(Long productId) {
        MallProductDetailDto detail = mallProductService.getProductAndDrugInfoById(productId);
        if (!Integer.valueOf(PRODUCT_STATUS_ON_SALE).equals(detail.getStatus())) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }
        return detail;
    }

    /**
     * 统一清洗文本参数。
     *
     * @param value 待清洗文本
     * @return 去除首尾空白后的文本，空白文本返回 null
     */
    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
