package cn.zhangchuangla.medicine.payment.config;

import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import cn.zhangchuangla.medicine.payment.service.impl.AlipayPaymentServiceImpl;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝支付模块的自动装配入口。
 * <p>
 * 通过 {@link EnableConfigurationProperties} 把 alipay 配置项绑定成 {@link AlipayProperties}，
 * 并在 Spring 容器中自动创建 {@link AlipayClient} 和 {@link AlipayPaymentService}。
 * 其他模块只需要在 pom.xml 中引入 medicine-payment 模块，就可以直接依赖这些 Bean。
 * </p>
 */
@Configuration
@ConditionalOnClass(AlipayClient.class)
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlipayAutoConfiguration.class);

    /**
     * 构建默认的 {@link AlipayClient} 实例。
     * <p>
     * 这里使用的是支付宝官方 SDK 提供的 {@link DefaultAlipayClient}，
     * 通过 Bean 的方式暴露给业务层使用。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayClient alipayClient(AlipayProperties properties) {
        LOGGER.info("初始化 AlipayClient，当前使用的网关地址为：{}", properties.getGatewayUrl());
        return new DefaultAlipayClient(
            properties.getGatewayUrl(),
            properties.getAppId(),
            properties.getPrivateKey(),
            properties.getFormat(),
            properties.getCharset(),
            properties.getAlipayPublicKey(),
            properties.getSignType()
        );
    }

    /**
     * 暴露封装好的支付宝支付服务，提供更高层的支付能力。
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayPaymentService alipayPaymentService(AlipayClient alipayClient,
                                                     AlipayProperties properties) {
        return new AlipayPaymentServiceImpl(alipayClient, properties);
    }
}
