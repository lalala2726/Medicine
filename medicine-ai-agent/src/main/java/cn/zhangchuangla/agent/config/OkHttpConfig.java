package cn.zhangchuangla.agent.config;

import cn.zhangchuangla.agent.interceptor.AgentResponseInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OkHttpConfig {

    /**
     * 连接建立超时，避免连接阻塞。
     */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * 读取响应超时，控制服务端返回耗时上限。
     */
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 写入请求超时，限制请求体上传耗时。
     */
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 请求总超时，防止长时间挂起。
     */
    private static final Duration CALL_TIMEOUT = Duration.ofSeconds(60);

    /**
     * OkHttp 客户端，统一挂载 FastAPI 响应拦截器做结果解包。
     */
    @Bean
    public OkHttpClient okHttpClient(AgentResponseInterceptor agentResponseInterceptor) {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(READ_TIMEOUT)
                .writeTimeout(WRITE_TIMEOUT)
                .callTimeout(CALL_TIMEOUT)
                .addInterceptor(agentResponseInterceptor)
                .build();
    }
}
