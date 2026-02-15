package cn.zhangchuangla.medicine.common.http.model;

import lombok.Builder;
import lombok.Getter;
import okhttp3.Headers;
import okhttp3.HttpUrl;

/**
 * @author Chuang
 * <p>
 * created on 2026/1/31
 */
@Getter
@Builder(builderClassName = "Builder", buildMethodName = "buildInternal")
public final class ClientRequest {

    private final HttpMethod method;
    private final HttpUrl url;
    private final Headers headers;
    private final String body;

    public static class Builder {

        private HttpUrl.Builder urlBuilder;
        private Headers.Builder headersBuilder;

        /**
         * 直接传入 URL 字符串并支持继续追加 query 参数。
         */
        public Builder url(String url) {
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("url must not be blank");
            }
            HttpUrl parsed = HttpUrl.parse(url);
            if (parsed == null) {
                throw new IllegalArgumentException("url is invalid: " + url);
            }
            this.urlBuilder = parsed.newBuilder();
            this.url = null;
            return this;
        }

        /**
         * 直接传入 HttpUrl 并支持继续追加 query 参数。
         */
        public Builder url(HttpUrl url) {
            if (url == null) {
                throw new IllegalArgumentException("url must not be null");
            }
            this.urlBuilder = url.newBuilder();
            this.url = null;
            return this;
        }

        /**
         * 追加 query 参数（需要先设置 url）。
         */
        public Builder addQueryParameter(String name, String value) {
            ensureUrlBuilder();
            this.urlBuilder.addQueryParameter(name, value);
            return this;
        }

        /**
         * 追加请求头（在原 headers 基础上追加）。
         */
        public Builder addHeader(String name, String value) {
            ensureHeadersBuilder();
            this.headersBuilder.add(name, value);
            return this;
        }

        /**
         * 构建 ClientRequest，确保 url 与 headers 最终一致。
         */
        public ClientRequest build() {
            if (urlBuilder != null) {
                this.url = urlBuilder.build();
            }
            if (headersBuilder != null) {
                this.headers = headersBuilder.build();
            }
            if (this.url == null) {
                throw new IllegalStateException("url must not be null");
            }
            return buildInternal();
        }

        private void ensureUrlBuilder() {
            if (this.urlBuilder == null) {
                if (this.url == null) {
                    throw new IllegalStateException("url must be set before adding query parameters");
                }
                this.urlBuilder = this.url.newBuilder();
                this.url = null;
            }
        }

        private void ensureHeadersBuilder() {
            if (this.headersBuilder == null) {
                this.headersBuilder = this.headers != null
                        ? this.headers.newBuilder()
                        : new Headers.Builder();
                this.headers = null;
            }
        }
    }
}
