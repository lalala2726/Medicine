package cn.zhangchuangla.medicine.client.elasticsearch.config;

import cn.zhangchuangla.medicine.common.elasticsearch.repository.support.NoIndexElasticsearchRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * 启用商城 Elasticsearch 仓储扫描。
 */
@Configuration
@EnableElasticsearchRepositories(
        basePackages = "cn.zhangchuangla.medicine.client.elasticsearch.repository",
        repositoryBaseClass = NoIndexElasticsearchRepository.class
)
public class ElasticsearchRepositoryConfig {
}
