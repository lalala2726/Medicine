package cn.zhangchuangla.medicine.agent.interceptor;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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

        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(bodyText);
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

        Integer code = jsonObject.getInteger(CODE_FIELD);
        if (code != null && code != SUCCESS) {
            String message = jsonObject.getString(MESSAGE_FIELD);
            String errorMessage = StringUtils.hasText(message) ? message : "Agent接口连接失败！请稍后再试～！";
            log.error("Agent接口连接失败！响应码：{}，响应体：{}", code, bodyText);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, errorMessage);
        }

        Object data = jsonObject.get(DATA_FIELD);
        String dataText = data == null ? "" : JSON.toJSONString(data);
        MediaType resolvedType = contentType != null ? contentType : DEFAULT_MEDIA_TYPE;
        return response.newBuilder()
                .body(ResponseBody.create(dataText, resolvedType))
                .build();
    }
}
