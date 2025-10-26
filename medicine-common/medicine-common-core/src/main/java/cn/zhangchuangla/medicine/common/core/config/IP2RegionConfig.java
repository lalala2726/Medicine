package cn.zhangchuangla.medicine.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IP2Region配置类
 *
 * @author Chuang
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ip2region")
public class IP2RegionConfig {

    /**
     * 是否启用IP地理位置功能
     */
    private boolean enabled = true;

    /**
     * IP数据库文件路径
     */
    private String dbPath = "classpath:data/ip2region.xdb";

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;
}
