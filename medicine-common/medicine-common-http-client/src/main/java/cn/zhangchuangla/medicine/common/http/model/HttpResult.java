package cn.zhangchuangla.medicine.common.http.model;

import lombok.Builder;
import lombok.Getter;
import okhttp3.Headers;

/**
 * 统一 HTTP 结果封装，包含状态码、响应头、原始内容与解析后的数据。
 *
 * @author Chuang
 * <p>
 * created on 2026/1/31
 */
@Getter
@Builder
public class HttpResult<T> {

    private final int statusCode;
    private final Headers headers;
    private final String body;
    private final T data;

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
