package cn.zhangchuangla.medicine.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认收货请求参数
 *
 * @author Chuang
 * created 2025/11/08
 */
@Data
@Schema(description = "确认收货请求参数")
public class OrderReceiveRequest {

    @Schema(description = "订单ID", example = "1")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}

