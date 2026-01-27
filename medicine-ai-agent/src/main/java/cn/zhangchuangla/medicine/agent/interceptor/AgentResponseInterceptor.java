package cn.zhangchuangla.medicine.agent.interceptor;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.JSONUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * FastAPI 统一响应体拦截器，提取 data 字段并处理错误码。
 */
@Component
@Slf4j
public class AgentResponseInterceptor implements Interceptor {

    private static final String CODE_FIELD = "code";
    private static final String MESSAGE_FIELD = "message";
    private static final String DATA_FIELD = "data";
    private static final int SUCCESS = 200;
    private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");


    /**
     * 响应拦截器
     */
    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        ResponseBody body = response.body();
        if (body == null) {
            return response;
        }

        MediaType contentType = body.contentType();
        String bodyText = body.string();
        if (!StringUtils.hasText(bodyText)) {
            return response.newBuilder()
                    .body(ResponseBody.create("", contentType))
                    .build();
        }

        JsonObject jsonObject;
        try {
            jsonObject = JSONUtils.parseObject(bodyText);
        } catch (Exception ex) {
            return response.newBuilder()
                    .body(ResponseBody.create(bodyText, contentType))
                    .build();
        }

        if (jsonObject == null) {
            return response.newBuilder()
                    .body(ResponseBody.create(bodyText, contentType))
                    .build();
        }

        Integer code = readInt(jsonObject, CODE_FIELD);
        if (code != null && code != SUCCESS) {
            String message = readString(jsonObject, MESSAGE_FIELD);
            String errorMessage = StringUtils.hasText(message) ? message : "Agent接口连接失败！请稍后再试～！";
            log.error("Agent接口连接失败！响应码：{}，响应体：{}", code, bodyText);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, errorMessage);
        }

        JsonElement data = jsonObject.get(DATA_FIELD);
        String dataText = data == null || data.isJsonNull() ? "" : JSONUtils.toJson(data);
        MediaType resolvedType = contentType != null ? contentType : DEFAULT_MEDIA_TYPE;
        return response.newBuilder()
                .body(ResponseBody.create(dataText, resolvedType))
                .build();
    }

    private Integer readInt(JsonObject jsonObject, String field) {
        JsonElement element = jsonObject.get(field);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            try {
                return Integer.parseInt(element.getAsString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String readString(JsonObject jsonObject, String field) {
        JsonElement element = jsonObject.get(field);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return JSONUtils.toJson(element);
    }
}
