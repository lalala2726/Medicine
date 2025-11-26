package cn.zhangchuangla.medicine.common.elasticsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * 启用公共 Elasticsearch 仓储扫描。
 */
@Configuration
@EnableElasticsearchRepositories(
        basePackages = "cn.zhangchuangla.medicine.common.elasticsearch.repository"
)
public class ElasticsearchRepositoryConfig {
}
