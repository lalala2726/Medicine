package cn.zhangchuangla.medicine.common.ip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IP2Region 配置项。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ip2region")
public class IP2RegionConfig {

    /**
     * 是否启用 IP 地理位置解析。
     */
    private boolean enabled = true;

    /**
     * IP 库文件路径，支持 classpath: 前缀和本地文件绝对/相对路径。
     */
    private String dbPath = "classpath:data/ip2region.xdb";

    /**
     * 是否启用内存缓存模式。
     */
    private boolean cacheEnabled = true;
}
