package cn.zhangchuangla.medicine.agent.config;

import cn.zhangchuangla.medicine.agent.interceptor.AgentResponseInterceptor;
import cn.zhangchuangla.medicine.agent.util.RequestSupport;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OkHttpConfig {

    /**
     * OkHttp 客户端，统一挂载 FastAPI 响应拦截器做结果解包。
     * 超时参数由 RequestSupport 统一维护。
     */
    @Bean
    public OkHttpClient okHttpClient(AgentResponseInterceptor agentResponseInterceptor) {
        return RequestSupport.newClientBuilder()
                .addInterceptor(agentResponseInterceptor)
                .build();
    }
}
