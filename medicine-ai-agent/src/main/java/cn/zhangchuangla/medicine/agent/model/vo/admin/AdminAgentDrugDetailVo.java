package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Admin 端智能体药品详情。
 */
@Schema(description = "Admin 端智能体药品详情")
public class AdminAgentDrugDetailVo {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "维生素C片")
    private String productName;

    @Schema(description = "药品说明信息")
    private DrugDetailDto drugDetail;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public DrugDetailDto getDrugDetail() {
        return drugDetail;
    }

    public void setDrugDetail(DrugDetailDto drugDetail) {
        this.drugDetail = drugDetail;
    }
}
