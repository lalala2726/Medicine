package cn.zhangchuangla.medicine.common.milvus.config;

import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Milvus 客户端配置
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MilvusProperties.class)
public class MilvusConfiguration {

    private final MilvusProperties milvusProperties;

    @Bean(destroyMethod = "close")
    public MilvusClientV2 milvusClientV2() {
        if (!StringUtils.hasText(milvusProperties.getUri())) {
            throw new ServiceException("Milvus 连接地址未配置");
        }

        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(milvusProperties.getUri())
                .token(milvusProperties.getToken())
                .dbName(milvusProperties.getDatabase())
                .build();
        return new MilvusClientV2(connectConfig);
    }
}
