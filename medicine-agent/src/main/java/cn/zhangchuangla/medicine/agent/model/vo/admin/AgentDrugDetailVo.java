package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentFieldDesc;
import cn.zhangchuangla.medicine.agent.annotation.AgentVoDesc;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端智能体药品详情。
 */
@Schema(description = "管理端智能体药品详情")
@AgentVoDesc("管理端智能体药品详情")
@Data
public class AgentDrugDetailVo {

    @Schema(description = "商品ID", example = "1")
    @AgentFieldDesc("商品ID")
    private Long productId;

    @Schema(description = "商品名称", example = "维生素C片")
    @AgentFieldDesc("商品名称")
    private String productName;

    @Schema(description = "药品说明信息")
    @AgentFieldDesc("药品说明信息")
    private DrugDetailDto drugDetail;
}
