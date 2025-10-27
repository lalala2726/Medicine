package cn.zhangchuangla.medicine.client.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 客户端OpenAPI(Swagger3)配置类
 *
 * @author Chuang
 */
@Configuration
public class ClientOpenAPIConfig {

    /**
     * 商城API组 - 负责商品展示、分类导航、搜索、购物车、优惠券等功能
     */
    @Bean
    public GroupedOpenApi mallApi() {
        return GroupedOpenApi.builder()
                .group("商城管理")
                .packagesToScan("cn.zhangchuangla.medicine.client.controller.mall")
                .build();
    }

    /**
     * 订单API组 - 负责下单、支付、退款、发货、订单状态跟踪
     */
    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("订单管理")
                .packagesToScan("cn.zhangchuangla.medicine.client.controller.order")
                .build();
    }

    /**
     * 用户API组 - 负责用户注册登录、信息维护、收货地址
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .packagesToScan("cn.zhangchuangla.medicine.client.controller.user")
                .build();
    }

    /**
     * AI咨询API组 - 负责药品问答、智能推荐、疾病分析等AI服务
     */
    @Bean
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("AI咨询")
                .packagesToScan("cn.zhangchuangla.medicine.client.controller.ai")
                .build();
    }

    /**
     * 消息通知API组 - 负责短信、模板消息、系统通知
     */
    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("消息通知")
                .packagesToScan("cn.zhangchuangla.medicine.client.controller.notification")
                .build();
    }

    /**
     * 通用功能API组
     */
    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
                .group("通用功能")
                .packagesToScan("cn.zhangchuangla.medicine.client.controller.common")
                .build();
    }

    /**
     * 所有接口
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("所有接口")
                .packagesToScan("cn.zhangchuangla.medicine.client.controller")
                .build();
    }

    /**
     * OpenAPI 主配置
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("客户端API接口文档")
                        .description("提供完整的客户端API接口定义，包括商城、订单、用户、AI咨询、消息通知等功能模块")
                        .contact(new Contact()
                                .name("Chuang")
                                .email("chuang@zhangchuangla.cn")
                                .url("https://opensource.org/licenses/apache-2-0")));
    }
}
