package cn.zhangchuangla.medicine.llm.model.dto;

import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
@Data
public class DrugInfoDto {

    /**
     * 药品通用名，例如“喉咙清颗粒”
     */
    private String commonName;

    /**
     * 成分（如“土牛膝、马兰草、车前草、天名精…”）
     */
    private String composition;

    /**
     * 性状（如“棕褐色的颗粒；味甜、微苦…”）
     */
    private String characteristics;

    /**
     * 包装规格（如“复合膜包装，12袋/盒”）
     */
    private String packaging;

    /**
     * 有效期（如“24个月”）
     */
    private String validityPeriod;

    /**
     * 贮藏条件（如“密封，置阴凉处（不超过20°C）”）
     */
    private String storageConditions;

    /**
     * 生产单位（如“湖南时代阳光药业股份有限公司”）
     */
    private String productionUnit;

    /**
     * 批准文号（如“国药准字Z20090802”）
     */
    private String approvalNumber;

    /**
     * 执行标准（如“国家食品药品监督管理局标准YBZ13322009”）
     */
    private String executiveStandard;

    /**
     * 产地类型（如“国产”或“进口”）
     */
    private String originType;

    /**
     * 是否外用药（如“否”）
     */
    private Boolean isOutpatientMedicine;

    /**
     * 温馨提示
     */
    private String warmTips;

    /**
     * 品牌名称
     */
    private String brand;

    /**
     * 是否处方药
     */
    private Boolean prescription;

    /**
     * 功能主治
     */
    private String efficacy;

    /**
     * 用法用量
     */
    private String usageMethod;

    /**
     * 不良反应
     */
    private String adverseReactions;

    /**
     * 注意事项
     */
    private String precautions;

    /**
     * 禁忌
     */
    private String taboo;

    /**
     * 药品说明书全文（可选）
     */
    private String instruction;

}
