package cn.zhangchuangla.agent.util;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用请求支持工具，封装 OkHttp 公共配置与参数构建逻辑。
 */
public final class RequestSupport {

    /**
     * 连接建立超时，避免连接阻塞。
     */
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    /**
     * 读取响应超时，控制服务端返回耗时上限。
     */
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    /**
     * 写入请求超时，限制请求体上传耗时。
     */
    public static final Duration WRITE_TIMEOUT = Duration.ofSeconds(30);
    /**
     * 请求总超时，防止长时间挂起。
     */
    public static final Duration CALL_TIMEOUT = Duration.ofSeconds(60);

    public static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private RequestSupport() {
    }

    public static OkHttpClient.Builder newClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(READ_TIMEOUT)
                .writeTimeout(WRITE_TIMEOUT)
                .callTimeout(CALL_TIMEOUT);
    }

    public static Request buildGetRequest(String url, Object query) {
        String requestUrl = buildUrl(url, query);
        return new Request.Builder()
                .url(requestUrl)
                .get()
                .build();
    }

    public static Request buildJsonRequest(String url, JsonMethod method, Object body) {
        RequestBody requestBody = jsonBody(body);
        Request.Builder builder = new Request.Builder().url(url);
        return switch (method) {
            case POST -> builder.post(requestBody).build();
            case PUT -> builder.put(requestBody).build();
            case DELETE -> builder.delete(requestBody).build();
        };
    }

    public static String execute(OkHttpClient client, Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throwNetworkError();
            }
            ResponseBody body = response.body();
            if (body == null) {
                throwNetworkError();
            }
            return body.string();
        } catch (IOException ex) {
            throwNetworkError();
        }
        return "";
    }

    public static RequestBody jsonBody(Object body) {
        String json = body instanceof String ? (String) body : JSON.toJSONString(body);
        return RequestBody.create(json, JSON_MEDIA_TYPE);
    }

    public static String buildUrl(String url, Object query) {
        if (query == null) {
            return url;
        }
        if (query instanceof String queryString) {
            if (queryString.isBlank()) {
                return url;
            }
            String separator = url.contains("?") ? "&" : "?";
            return url + separator + queryString;
        }
        HttpUrl parsedUrl = HttpUrl.parse(url);
        if (parsedUrl == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "请求地址不合法");
        }
        HttpUrl.Builder builder = parsedUrl.newBuilder();
        for (Map.Entry<String, Object> entry : toMap(query).entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                builder.addQueryParameter(entry.getKey(), String.valueOf(value));
            }
        }
        return builder.build().toString();
    }

    public static Map<String, Object> toMap(Object query) {
        if (query instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return result;
        }
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(query));
        Map<String, Object> result = new LinkedHashMap<>();
        if (jsonObject != null) {
            for (String key : jsonObject.keySet()) {
                result.put(key, jsonObject.get(key));
            }
        }
        return result;
    }

    public static void throwNetworkError() {
        throw new ServiceException(ResponseCode.OPERATION_ERROR, "网络错误，请稍后重试");
    }

    public enum JsonMethod {
        POST,
        PUT,
        DELETE
    }
}
