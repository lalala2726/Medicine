package cn.zhangchuangla.medicine.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/22
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .setDefaultVersion("1.0")       // 默认版本
                .setVersionRequired(false);     // 关闭“必须有版本”
    }
}
