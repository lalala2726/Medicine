package cn.zhangchuangla.medicine.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI(Swagger3)配置类
 *
 * @author Chuang
 */
@Configuration
public class AdminOpenAPIConfig {

    /**
     * 系统接口组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .packagesToScan("cn.zhangchuangla.medicine.admin.controller.system")
                .build();
    }

    /**
     * 商城接口组
     */
    @Bean
    public GroupedOpenApi monitorApi() {
        return GroupedOpenApi.builder()
                .group("商城管理")
                .packagesToScan("cn.zhangchuangla.medicine.admin.controller.mall")
                .build();
    }


    /**
     * 通用接口组
     */
    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
                .group("通用功能")
                .packagesToScan("cn.zhangchuangla.medicine.admin.controller.common")
                .build();
    }


    /**
     * 认证接口
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("认证接口")
                .packagesToScan("cn.zhangchuangla.medicine.admin.controller.auth")
                .build();
    }

    /**
     * 药品管理
     */
    @Bean
    public GroupedOpenApi toolApi() {
        return GroupedOpenApi.builder()
                .group("药品管理")
                .packagesToScan("cn.zhangchuangla.medicine.admin.controller.medicine")
                .build();
    }


    /**
     * 所有接口
     */
    @Bean
    public GroupedOpenApi personal() {
        return GroupedOpenApi.builder()
                .group("所有接口")
                .packagesToScan("cn.zhangchuangla.medicine.admin.controller")
                .build();
    }


    /**
     * OpenAPI 主配置
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .description("提供完整的API接口定义与交互说明，便于快速集成和使用。")
                        .contact(new Contact()
                                .name("Chuang")
                                .email("chuang@zhangchuangla.cn")
                                .name("Apache 2.0")
                                .url("https://opensource.org/licenses/apache-2-0")));
    }
}
