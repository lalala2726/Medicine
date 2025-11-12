package cn.zhangchuangla.medicine.common.core.entity;

/**
 * @author Chuang
 * <p>
 * created on 2025/4/17
 */
public class IPEntity {

    /**
     * IP地址
     */
    private String ip;

    /**
     * 国家
     */
    private String country;

    /**
     * 区域
     */
    private String area;

    /**
     * 运营商
     */
    private String ISP;


    /**
     * 位置详情
     */
    private String region;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getISP() {
        return ISP;
    }

    public void setISP(String ISP) {
        this.ISP = ISP;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
