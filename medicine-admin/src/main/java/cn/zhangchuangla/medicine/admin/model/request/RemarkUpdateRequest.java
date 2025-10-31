package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/1 02:07
 */
@Data
@Schema(description = "订单备注更新参数")
public class RemarkUpdateRequest {

    /**
     * 订单号
     */
    @Schema(description = "订单号")
    @NotNull(message = "订单号不能为空")
    private String orderNo;

    /**
     * 订单备注
     */
    @Schema(description = "订单备注")
    private String remark;

}
