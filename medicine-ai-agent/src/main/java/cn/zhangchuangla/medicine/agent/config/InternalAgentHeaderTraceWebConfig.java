package cn.zhangchuangla.medicine.agent.config;

import cn.zhangchuangla.medicine.agent.interceptor.InternalAgentHeaderTraceInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 内部智能体请求头追踪拦截器注册。
 */
@Configuration
@ConditionalOnProperty(
        prefix = "agent.header-trace",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class InternalAgentHeaderTraceWebConfig implements WebMvcConfigurer {

    private final InternalAgentHeaderTraceInterceptor internalAgentHeaderTraceInterceptor;

    public InternalAgentHeaderTraceWebConfig(InternalAgentHeaderTraceInterceptor internalAgentHeaderTraceInterceptor) {
        this.internalAgentHeaderTraceInterceptor = internalAgentHeaderTraceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalAgentHeaderTraceInterceptor)
                .addPathPatterns("/**");
    }
}
