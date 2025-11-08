package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 售后时间线视图对象
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "售后时间线")
public class AfterSaleTimelineVo {

    /**
     * 时间线ID
     */
    @Schema(description = "时间线ID", example = "1")
    private Long id;

    /**
     * 售后ID
     */
    @Schema(description = "事件类型", example = "REFUND")
    private String eventType;

    /**
     * 事件类型名称
     */
    @Schema(description = "事件类型名称", example = "退款申请")
    private String eventTypeName;

    /**
     * 事件状态
     */
    @Schema(description = "事件状态", example = "PROCESSING")
    private String eventStatus;

    /**
     * 操作人类型
     */
    @Schema(description = "操作人类型", example = "CUSTOMER")
    private String operatorType;

    /**
     * 操作人类型名称
     */
    @Schema(description = "操作人类型名称", example = "客户")
    private String operatorTypeName;

    /**
     * 事件描述
     */
    @Schema(description = "事件描述", example = "用户申请退款")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-08 10:00:00")
    private Date createTime;
}

