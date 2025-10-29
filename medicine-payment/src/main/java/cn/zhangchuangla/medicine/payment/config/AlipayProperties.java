package cn.zhangchuangla.medicine.payment.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 支付宝基础配置项。
 * <p>
 * 这里通过 Spring Boot 的 {@link ConfigurationProperties} 机制把 application.yml 中的 alipay.*
 * 配置自动绑定到 Java 对象上，方便后续在代码里引用。
 * </p>
 */
@Validated
@Data
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /**
     * 应用的 AppId，如沙箱环境在开放平台控制台可以查看。
     */
    @NotBlank(message = "请配置 alipay.appId（支付宝的应用 ID）")
    private String appId;

    /**
     * 商户私钥，用于请求支付宝时对报文进行签名。
     */
    @NotBlank(message = "请配置 alipay.private-key（商户私钥）")
    private String privateKey;

    /**
     * 支付宝公钥，用于回调验签。
     */
    @NotBlank(message = "请配置 alipay.alipay-public-key（支付宝公钥）")
    private String alipayPublicKey;

    /**
     * 支付宝开放平台网关地址，沙箱环境默认为 https://openapi-sandbox.dl.alipaydev.com/gateway.do。
     */
    @NotBlank(message = "请配置 alipay.gatewayUrl（支付宝网关地址）")
    private String gatewayUrl;

    /**
     * 默认的异步通知地址，可以被业务代码覆盖。
     */
    private String notifyUrl;

    /**
     * 默认的同步返回地址，可以被业务代码覆盖。
     */
    private String returnUrl;

    /**
     * 加签算法类型，通常使用 RSA2。
     */
    @NotBlank(message = "请配置 alipay.signType（签名类型，例如 RSA2）")
    private String signType = "RSA2";

    /**
     * 请求的字符编码，支付宝要求 UTF-8。
     */
    @NotBlank(message = "请配置 alipay.charset（字符编码，例如 utf-8）")
    private String charset = "utf-8";

    /**
     * 报文格式固定为 json，因此不对外暴露成配置项。
     */
    private String format = "json";

}
