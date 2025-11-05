package cn.zhangchuangla.medicine.client.model.request;

import cn.zhangchuangla.medicine.common.core.base.BasePageRequest;
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
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;
}
