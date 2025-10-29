package cn.zhangchuangla.medicine.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/30 04:03
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 页面跳转地址
     */
    private String returnUrl;

    /**
     * 网关地址
     */
    private String gatewayUrl;

    /**
     * 字符集
     */
    private String charset;

    /**
     * 签名方式
     */
    private String signType;
}
