package cn.zhangchuangla.medicine.admin.model.vo;

import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 药品详情视图对象
 *
 * @author Chuang
 * created on 2026/2/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent药品详情视图对象")
public class AgentDrugDetailVo {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "维生素C片")
    private String productName;

    @Schema(description = "药品说明信息")
    private DrugDetailDto drugDetail;
}
