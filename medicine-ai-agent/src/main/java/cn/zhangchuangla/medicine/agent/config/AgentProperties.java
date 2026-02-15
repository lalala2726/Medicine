package cn.zhangchuangla.medicine.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Chuang
 * <p>
 * created on 2026/1/26
 */
@Configuration
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    /**
     * 智能体后端基础地址，用于拼接 URLConstant 的接口路径。
     */
    private String url = "http://localhost:8000";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
