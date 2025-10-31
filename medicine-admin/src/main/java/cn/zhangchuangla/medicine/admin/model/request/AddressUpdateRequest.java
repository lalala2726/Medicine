package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/1 02:04
 */
@Data
@Schema(description = "地址更新参数")
public class AddressUpdateRequest {

    /**
     * 订单编号
     */
    @Schema(description = "订单编号")
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    /**
     * 收货人姓名
     */
    @Schema(description = "收货人姓名")
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    /**
     * 收货人电话
     */
    @Schema(description = "收货人电话")
    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    /**
     * 收货人地址
     */
    @Schema(description = "收货人地址")
    @NotBlank(message = "收货人地址不能为空")
    private String receiverDetail;

    /**
     * 配送方式
     */
    @Schema(description = "配送方式")
    @NotBlank(message = "配送方式不能为空")
    private String deliveryType;


}
