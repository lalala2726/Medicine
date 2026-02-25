package cn.zhangchuangla.medicine.dubbo.api.model;

import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理端智能体药品详情。
 */
@Data
public class AgentDrugDetailDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long productId;

    private String productName;

    private DrugDetailDto drugDetail;
}
