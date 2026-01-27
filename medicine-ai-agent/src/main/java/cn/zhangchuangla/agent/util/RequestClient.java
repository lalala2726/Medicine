package cn.zhangchuangla.agent.util;

import cn.zhangchuangla.agent.interceptor.AgentResponseInterceptor;
import okhttp3.OkHttpClient;

/**
 * 统一 FastAPI 调用客户端，自动解包通用返回体的 data 字段。
 */
public final class RequestClient {

    private static final OkHttpClient CLIENT = RequestSupport.newClientBuilder()
            .addInterceptor(new AgentResponseInterceptor())
            .build();

    private RequestClient() {
    }

    /**
     * GET 请求，query 支持 Map 或对象，最终拼接为 URL 查询参数。
     */
    public static String get(String url, Object query) {
        return RequestSupport.execute(CLIENT, RequestSupport.buildGetRequest(url, query));
    }

    /**
     * POST JSON 请求，body 会序列化为 JSON。
     */
    public static String post(String url, Object body) {
        return RequestSupport.execute(
                CLIENT,
                RequestSupport.buildJsonRequest(url, RequestSupport.JsonMethod.POST, body)
        );
    }

    /**
     * PUT JSON 请求，body 会序列化为 JSON。
     */
    public static String put(String url, Object body) {
        return RequestSupport.execute(
                CLIENT,
                RequestSupport.buildJsonRequest(url, RequestSupport.JsonMethod.PUT, body)
        );
    }

    /**
     * DELETE JSON 请求，body 会序列化为 JSON。
     */
    public static String delete(String url, Object body) {
        return RequestSupport.execute(
                CLIENT,
                RequestSupport.buildJsonRequest(url, RequestSupport.JsonMethod.DELETE, body)
        );
    }
}
