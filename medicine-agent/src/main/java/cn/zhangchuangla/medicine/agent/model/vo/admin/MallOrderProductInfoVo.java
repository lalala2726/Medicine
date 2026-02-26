package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentFieldDesc;
import cn.zhangchuangla.medicine.agent.annotation.AgentVoDesc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端智能体订单商品信息视图。
 */
@Schema(description = "管理端智能体订单商品信息")
@AgentVoDesc("管理端智能体订单商品信息")
@Data
public class MallOrderProductInfoVo {

    @Schema(description = "商品名称", example = "商品名称")
    @AgentFieldDesc("商品名称")
    private String productName;

    @Schema(description = "商品图片", example = "商品图片")
    @AgentFieldDesc("商品图片")
    private String productImage;

    @Schema(description = "商品价格", example = "100.00")
    @AgentFieldDesc("商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品分类")
    @AgentFieldDesc("商品分类")
    private String productCategory;

    @Schema(description = "商品ID", example = "1")
    @AgentFieldDesc("商品ID")
    private Long productId;

    @Schema(description = "商品数量", example = "1")
    @AgentFieldDesc("商品数量")
    private Integer quantity;
}
