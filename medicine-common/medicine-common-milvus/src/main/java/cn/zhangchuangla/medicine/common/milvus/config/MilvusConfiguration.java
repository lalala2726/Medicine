package cn.zhangchuangla.medicine.common.milvus.config;

import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "milvus", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MilvusConfiguration {

    private final MilvusProperties milvusProperties;

    @Bean(destroyMethod = "close")
    public MilvusServiceClient milvusServiceClient() {
        if (!StringUtils.hasText(milvusProperties.getUri())) {
            throw new ServiceException("Milvus 连接地址未配置");
        }

        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withUri(milvusProperties.getUri());
        if (StringUtils.hasText(milvusProperties.getToken())) {
            builder.withToken(milvusProperties.getToken());
        }
        if (StringUtils.hasText(milvusProperties.getDatabase())) {
            builder.withDatabaseName(milvusProperties.getDatabase());
        }
        return new MilvusServiceClient(builder.build());
    }

    @Bean(destroyMethod = "close")
    public MilvusClientV2 milvusClientV2() {
        if (!StringUtils.hasText(milvusProperties.getUri())) {
            throw new ServiceException("Milvus 连接地址未配置");
        }

        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                .uri(milvusProperties.getUri());
        if (StringUtils.hasText(milvusProperties.getToken())) {
            builder.token(milvusProperties.getToken());
        }
        if (StringUtils.hasText(milvusProperties.getDatabase())) {
            builder.dbName(milvusProperties.getDatabase());
        }
        return new MilvusClientV2(builder.build());
    }
}
