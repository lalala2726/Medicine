package cn.zhangchuangla.medicine.common.elasticsearch.config;

import cn.zhangchuangla.medicine.common.elasticsearch.document.MallProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * 初始化商城商品索引，确保自定义分析器设置生效。
 */
@Configuration
@RequiredArgsConstructor
public class MallProductIndexConfiguration {

    private final ElasticsearchOperations elasticsearchOperations;

    @Bean
    public CommandLineRunner initMallProductIndex() {
        return args -> {
            var indexOps = elasticsearchOperations.indexOps(MallProductDocument.class);
            if (!indexOps.exists()) {
                indexOps.create();
                indexOps.putMapping(indexOps.createMapping());
            }
        };
    }
}
