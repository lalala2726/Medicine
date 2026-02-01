package cn.zhangchuangla.medicine.common.http.model;

import cn.zhangchuangla.medicine.common.core.utils.JSONUtils;
import com.google.gson.reflect.TypeToken;
import lombok.Data;

import java.lang.reflect.Type;

/**
 * @author Chuang
 * <p>
 * created on 2026/2/1
 */
@Data
public class BaseResponse<T> {

    private int code;

    private String message;

    private T data;

    /**
     * 根据 dataClass 解析 JSON 为 BaseResponse。
     */
    public static <T> BaseResponse<T> fromJson(String json, Class<T> dataClass) {
        Type type = TypeToken.getParameterized(BaseResponse.class, dataClass).getType();
        return JSONUtils.fromJson(json, type);
    }

    /**
     * 根据 dataType 解析 JSON 为 BaseResponse（支持泛型）。
     */
    public static <T> BaseResponse<T> fromJson(String json, Type dataType) {
        Type type = TypeToken.getParameterized(BaseResponse.class, dataType).getType();
        return JSONUtils.fromJson(json, type);
    }
}
