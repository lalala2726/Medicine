package cn.zhangchuangla.medicine.common.ip.entity;

import lombok.Data;

/**
 * IP 归属地结构化信息。
 */
@Data
public class IPEntity {

    /**
     * 原始 IP 地址。
     */
    private String ip;

    /**
     * 国家信息。
     */
    private String country;

    /**
     * 省/州信息。
     */
    private String area;

    /**
     * 运营商信息。
     */
    private String ISP;

    /**
     * 详细地区信息。
     */
    private String region;
}
