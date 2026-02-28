package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.model.request.AfterSaleListRequest;
import cn.zhangchuangla.medicine.admin.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.model.dto.AfterSaleDetailDto;
import cn.zhangchuangla.medicine.model.dto.AfterSaleTimelineDto;
import cn.zhangchuangla.medicine.model.dto.MallAfterSaleListDto;
import cn.zhangchuangla.medicine.model.request.MallAfterSaleListRequest;
import cn.zhangchuangla.medicine.model.vo.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.AfterSaleTimelineVo;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentAfterSaleRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 管理端 Agent 售后 RPC Provider。
 */
@DubboService(interfaceClass = AdminAgentAfterSaleRpcService.class, group = "medicine-admin", version = "1.0.0")
@RequiredArgsConstructor
public class AdminAgentAfterSaleRpcServiceImpl implements AdminAgentAfterSaleRpcService {

    private final MallAfterSaleService mallAfterSaleService;

    /**
     * 功能描述：提供给 Agent 端的售后列表分页查询能力。
     *
     * @param query 售后分页查询参数，包含分页参数与筛选条件
     * @return 返回售后分页结果，记录类型为 {@link MallAfterSaleListDto}
     * @throws RuntimeException 异常说明：当管理端售后服务查询异常时抛出运行时异常
     */
    @Override
    public Page<MallAfterSaleListDto> listAfterSales(MallAfterSaleListRequest query) {
        AfterSaleListRequest afterSaleListRequest = BeanCotyUtils.copyProperties(query, AfterSaleListRequest.class);
        return mallAfterSaleService.getAfterSaleList(afterSaleListRequest);
    }

    /**
     * 功能描述：根据售后申请 ID 查询售后详情并返回给 Agent。
     *
     * @param afterSaleId 售后申请 ID
     * @return 返回售后详情 DTO 对象
     * @throws RuntimeException 异常说明：当售后详情查询异常时抛出运行时异常
     */
    @Override
    public AfterSaleDetailDto getAfterSaleDetailById(Long afterSaleId) {
        AfterSaleDetailVo detailVo = mallAfterSaleService.getAfterSaleDetail(afterSaleId);
        return toAfterSaleDetailDto(detailVo);
    }

    /**
     * 功能描述：将售后详情 VO 转换为 RPC 传输 DTO，避免 RPC 层直接暴露 VO。
     *
     * @param source 源售后详情 VO，类型为 {@link AfterSaleDetailVo}
     * @return 返回售后详情 DTO；当 source 为空时返回 null
     * @throws RuntimeException 异常说明：当属性转换异常时抛出运行时异常
     */
    private AfterSaleDetailDto toAfterSaleDetailDto(AfterSaleDetailVo source) {
        if (source == null) {
            return null;
        }
        AfterSaleDetailDto target = BeanCotyUtils.copyProperties(source, AfterSaleDetailDto.class);
        target.setProductInfo(toProductInfoDto(source.getProductInfo()));
        target.setTimeline(toTimelineDtos(source.getTimeline()));
        return target;
    }

    /**
     * 功能描述：将售后详情中的商品信息 VO 转换为 DTO。
     *
     * @param source 源商品信息 VO，类型为 {@link AfterSaleDetailVo.ProductInfo}
     * @return 返回商品信息 DTO；当 source 为空时返回 null
     * @throws RuntimeException 异常说明：当属性转换异常时抛出运行时异常
     */
    private AfterSaleDetailDto.ProductInfo toProductInfoDto(AfterSaleDetailVo.ProductInfo source) {
        return BeanCotyUtils.copyProperties(source, AfterSaleDetailDto.ProductInfo.class);
    }

    /**
     * 功能描述：将售后时间线 VO 列表转换为 DTO 列表。
     *
     * @param source 源时间线 VO 列表，元素类型为 {@link AfterSaleTimelineVo}
     * @return 返回时间线 DTO 列表；当 source 为空时返回空列表
     * @throws RuntimeException 异常说明：当属性转换异常时抛出运行时异常
     */
    private List<AfterSaleTimelineDto> toTimelineDtos(List<AfterSaleTimelineVo> source) {
        return BeanCotyUtils.copyListProperties(source, AfterSaleTimelineDto.class);
    }
}
