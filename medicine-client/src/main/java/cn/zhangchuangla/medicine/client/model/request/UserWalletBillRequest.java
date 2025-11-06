package cn.zhangchuangla.medicine.client.model.request;

import cn.zhangchuangla.medicine.common.core.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/6 06:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserWalletBillRequest extends BasePageRequest {

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2025-11-06 06:46:00")
    private Date startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2025-11-06 06:46:00")
    private Date endTime;
}
