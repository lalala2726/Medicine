package cn.zhangchuangla.medicine.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/9 15:37
 */
@Data
public class CurrentModel implements Serializable {

    /**
     * 厂商
     */
    private String provider;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 最大token数
     */
    private String maxTokens;

    /**
     * 模型温度
     */
    private String temperature;
}
