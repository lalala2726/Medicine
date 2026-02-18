package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端智能体订单列表视图。
 */
@Schema(description = "管理端智能体订单列表")
@Data
public class AdminMallOrderListVo {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单编号（业务唯一标识）", example = "O202510312122")
    private String orderNo;

    @Schema(description = "订单总金额（含运费）", example = "100.00")
    private BigDecimal totalAmount;

    @Schema(description = "支付方式（value-编码，description-描述）")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_ORDER_PAY_TYPE)
    private String payType;

    @Schema(description = "订单状态（value-编码，description-描述）")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_ORDER_STATUS)
    private String orderStatus;

    @Schema(description = "支付时间", example = "2025-10-31 21:22:00")
    private Date payTime;

    @Schema(description = "创建时间", example = "2025-10-31 21:22:00")
    private Date createTime;

    @Schema(description = "商品信息")
    private AdminMallOrderProductInfoVo productInfo;
}
