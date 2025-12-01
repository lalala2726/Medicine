package cn.zhangchuangla.medicine.llm.model.tool.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单状态分布")
public class StatusDistributionSnapshotTool {

    @Schema(description = "订单状态编码")
    private String status;

    @Schema(description = "订单状态名称")
    private String statusName;

    @Schema(description = "数量")
    private Long count;
}
